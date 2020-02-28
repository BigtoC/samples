package net.corda.ct.flow

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.commands.MoveTokenCommand
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.amount
import com.r3.corda.lib.tokens.workflows.flows.issue.addIssueTokens
import com.r3.corda.lib.tokens.workflows.utilities.addTokenTypeJar
import com.r3.corda.lib.tokens.workflows.utilities.firstNotary
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.toHexString
import net.corda.core.utilities.unwrap
import net.corda.ct.contract.ConfidentialAmount
import net.corda.ct.contract.ConfidentialToken
import net.corda.ct.contract.ConfidentialTokenSchemaV1
import net.corda.ct.crypto.CryptoParameters
import net.corda.ct.crypto.PedersenCommitment
import java.math.BigInteger
import java.util.*

@StartableByRPC
@InitiatingFlow
class IssueAndMoveConfidentialTokensFlow(
        private val recipient: Party,
        private val issueQuantity: Long = 100,
        private val moveQuantity: Long = 10
) : FlowLogic<SignedTransaction>() {

    private val myIdentity get() = serviceHub.myInfo.legalIdentities.first()
    private val defaultIssuer get() = myIdentity
    private val defaultIssuedTokenType: IssuedTokenType
        get() {
            val currency = Currency.getInstance("GBP")
            val GBP = TokenType(currency.currencyCode, currency.defaultFractionDigits)
            return IssuedTokenType(defaultIssuer, GBP)
        }

    init {
        require(moveQuantity <= issueQuantity) { "Cannot move a larger quantity than issued" }
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val (issueTransaction, tokenSecret) = issueToken()
        val confidentialToken = issueTransaction.tx.outRef<ConfidentialToken>(0)
        moveToken(confidentialToken, issueQuantity, tokenSecret)
        return issueTransaction
    }

    private fun issueToken(): Pair<SignedTransaction, BigInteger> {
        logger.info("Issuing a confidential token for quantity: $issueQuantity")

        // Create a token with a confidential amount
        val issueSecret = CryptoParameters.generateSecret()
        val issueConfidentialAmount = ConfidentialAmount.generate(amount(issueQuantity, defaultIssuedTokenType), issueSecret)
        val issueToken = ConfidentialToken(issueConfidentialAmount, myIdentity)

        val transactionBuilder = TransactionBuilder(notary = getPreferredNotary(serviceHub, firstNotary()))
        addIssueTokens(transactionBuilder, listOf(issueToken))
        addTokenTypeJar(listOf(issueToken), transactionBuilder)

        val issueTransaction = serviceHub.signInitialTransaction(transactionBuilder)
        serviceHub.recordTransactions(issueTransaction)

        // Store clear-text quantity and secret in the database
        serviceHub.updateState(issueTransaction.tx.outRef<ConfidentialToken>(0).ref, issueQuantity, issueSecret)

        return issueTransaction to issueSecret
    }


    @Suspendable
    private fun moveToken(inputToken: StateAndRef<ConfidentialToken>, inputTokenQuantity: Long, inputTokenSecret: BigInteger) {
        logger.info("Moving quantity $moveQuantity to $recipient")
        val session = initiateFlow(recipient)

        // Create token output state for the recipient
        val moveSecret = CryptoParameters.generateSecret()
        val confidentialMoveAmount = ConfidentialAmount.generate(amount(moveQuantity, defaultIssuedTokenType), moveSecret)
        val movedToken = ConfidentialToken(confidentialMoveAmount, recipient, inputToken.state.data.tokenTypeJarHash)

        val transactionBuilder = TransactionBuilder(notary = inputToken.state.notary)
                .addInputState(inputToken)
                .addOutputState(movedToken)
                .addCommand(MoveTokenCommand(defaultIssuedTokenType, listOf(0), listOf(0, 1)), myIdentity.owningKey)
        addTokenTypeJar(movedToken, transactionBuilder)

        // Create the remainder output state for ourselves
        val remainderQuantity = inputTokenQuantity - moveQuantity
        val remainderSecret = inputTokenSecret - moveSecret
        if (remainderQuantity > 0) {
            val confidentialRemainderAmount = ConfidentialAmount.generate(amount(remainderQuantity, defaultIssuedTokenType), remainderSecret)
            val remainderToken = ConfidentialToken(confidentialRemainderAmount, myIdentity, inputToken.state.data.tokenTypeJarHash)
            transactionBuilder.addOutputState(remainderToken)
        }

        // Notarise and send transaction to recipient
        val moveTransaction = serviceHub.signInitialTransaction(transactionBuilder)
        subFlow(FinalityFlow(moveTransaction, session))

        // Send clear-text quantity and secret so recipient can verify what quantity they received
        session.send(QuantityAndSecret(moveQuantity, moveSecret))

        // Store clear-text quantity and secret in the database
        serviceHub.updateState(moveTransaction.tx.outRef<ConfidentialToken>(1).ref, remainderQuantity, remainderSecret)
    }
}

fun ServiceHub.updateState(stateRef: StateRef, clearTextQuantity: Long, secret: BigInteger) {
    withEntityManager {
        val query = criteriaBuilder.createQuery(ConfidentialTokenSchemaV1.PersistentConfidentialToken::class.java)
        val type = query.from(ConfidentialTokenSchemaV1.PersistentConfidentialToken::class.java)
        query.select(type)
        val recordedState = createQuery(query).resultList.single {
            it.stateRef!!.txId == stateRef.txhash.bytes.toHexString() && it.stateRef!!.index == stateRef.index
        }

        recordedState.amount = clearTextQuantity
        recordedState.secret = secret.toString()
        persist(recordedState)
    }
}

@InitiatedBy(IssueAndMoveConfidentialTokensFlow::class)
class ReceiveConfidentialTokensFlow(private val otherSession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val stx = subFlow(ReceiveFinalityFlow(otherSession))
        val (quantity, secret) = otherSession.receive<QuantityAndSecret>().unwrap { it }
        val receivedToken = stx.tx.findOutput<ConfidentialToken> { it.holder == serviceHub.myInfo.legalIdentities.first() }
        verifyCorrectAmount(receivedToken, quantity, secret)
        serviceHub.updateState(stx.tx.outRef<ConfidentialToken>(0).ref, quantity, secret)

    }

    private fun verifyCorrectAmount(token: ConfidentialToken, quantity: Long, secret: BigInteger) {
        logger.info("Verifying if correct amount received for $token")
        val expectedAmount = amount(quantity, token.amount.token)
        val expected = ConfidentialAmount.generate(expectedAmount, secret)

        require(token.amount == expected) {
            "The received confidential amount does not verify against the claimed amount"
        }
        logger.info("Amount verifies! Received $expectedAmount")
    }
}

@CordaSerializable
data class QuantityAndSecret(val quantity: Long, val secret: BigInteger)
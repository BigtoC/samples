package net.corda.ct.contract

import com.r3.corda.lib.tokens.contracts.AbstractTokenContract
import com.r3.corda.lib.tokens.contracts.commands.TokenCommand
import com.r3.corda.lib.tokens.contracts.states.AbstractToken
import net.corda.core.contracts.*
import net.corda.core.internal.uncheckedCast
import net.corda.ct.crypto.PedersenCommitment
import java.security.PublicKey

open class ConfidentialTokenContract : AbstractTokenContract<ConfidentialToken>(), Contract {
    override val accepts: Class<ConfidentialToken> get() = uncheckedCast(ConfidentialToken::class.java)

    companion object {
        val contractId = this::class.java.enclosingClass.canonicalName
    }

    override fun verifyIssue(
            issueCommand: CommandWithParties<TokenCommand>,
            inputs: List<IndexedState<ConfidentialToken>>,
            outputs: List<IndexedState<ConfidentialToken>>,
            attachments: List<Attachment>,
            references: List<StateAndRef<ContractState>>
    ) {
        require(inputs.isEmpty()) { "When issuing tokens, there cannot be any input states." }
        outputs.apply {
            require(isNotEmpty()) { "When issuing tokens, there must be output states." }
            // We don't care about the token as the grouping function ensures that all the outputs are of the same
            // token.

            // TODO: When issuing tokens an amount > ZERO must be issued.

            // There can only be one issuer per group as the issuer is part of the token which is used to group states.
            // If there are multiple issuers for the same tokens then there will be a group for each issued token. So,
            // the line below should never fail on single().
            val issuerKey: PublicKey = this.map { it.state.data }.map(AbstractToken::issuer).toSet().single().owningKey
            val issueSigners: List<PublicKey> = issueCommand.signers
            // The issuer should be signing the issue command. Notice that it can be signed by more parties.
            require(issuerKey in issueSigners) {
                "The issuer must be the signing party when an amount of tokens are issued."
            }
        }

    }

    override fun verifyMove(
            moveCommands: List<CommandWithParties<TokenCommand>>,
            inputs: List<IndexedState<ConfidentialToken>>,
            outputs: List<IndexedState<ConfidentialToken>>,
            attachments: List<Attachment>,
            references: List<StateAndRef<ContractState>>
    ) {
        // There must be inputs and outputs present.
        require(inputs.isNotEmpty()) { "When moving tokens, there must be input states present." }
        require(outputs.isNotEmpty()) { "When moving tokens, there must be output states present." }

        // Sum the confidential amount of input and output tokens.
        val inputConfidentialAmountSum: PedersenCommitment = inputs
                .map { it.state.data.amount.hiddenQuantity }
                .reduce(PedersenCommitment::plus)

        val outputConfidentialAmountSum: PedersenCommitment = outputs
                .map { it.state.data.amount.hiddenQuantity }
                .reduce(PedersenCommitment::plus)

        // Input and output confidential amounts must be equal.
        require(inputConfidentialAmountSum == outputConfidentialAmountSum) {
            "In move groups the amount of input tokens MUST EQUAL the amount of output tokens. In other words, you " +
                    "cannot create or destroy value when moving tokens."
        }

        // TODO: All input & output amounts have to be > ZERO.

        // There can be different owners in each move group. There may be one command for each of the signers publickey
        // or all the public keys might be listed within one command.
        val inputOwningKeys: Set<PublicKey> = inputs.map { it.state.data.holder.owningKey }.toSet()
        val signers: Set<PublicKey> = moveCommands.flatMap(CommandWithParties<TokenCommand>::signers).toSet()
        require(signers.containsAll(inputOwningKeys)) {
            "Required signers does not contain all the current owners of the tokens being moved"
        }
    }

    override fun verifyRedeem(
            redeemCommand: CommandWithParties<TokenCommand>,
            inputs: List<IndexedState<ConfidentialToken>>,
            outputs: List<IndexedState<ConfidentialToken>>,
            attachments: List<Attachment>,
            references: List<StateAndRef<ContractState>>
    ) {
        // TODO: add redeem support
//        val issuedToken: IssuedTokenType = redeemCommand.value.token
//        // There can be at most one output treated as a change paid back to the owner. Issuer is used to group states,
//        // so it will be the same as one for the input states.
//        outputs.apply {
//            require(size <= 1) { "When redeeming tokens, there must be zero or one output state." }
//            if (isNotEmpty()) {
//                val amount = single().state.data.amount
//                require(amount > Amount.zero(issuedToken)) { "If there is an output, it must have a value greater than zero." }
//            }
//            // Outputs can be paid to any anonymous public key, so we cannot compare keys here.
//        }
//        inputs.apply {
//            // There must be inputs present.
//            require(isNotEmpty()) { "When redeeming tokens, there must be input states present." }
//            // We don't care about the token as the grouping function ensures all the inputs are of the same token.
//            val inputSum: Amount<IssuedTokenType> = this.map { it.state.data }.sumTokenStatesOrZero(issuedToken)
//            require(inputSum > Amount.zero(issuedToken)) {
//                "When redeeming tokens an amount > ZERO must be redeemed."
//            }
//            val outSum: Amount<IssuedTokenType> = outputs.firstOrNull()?.state?.data?.amount
//                    ?: Amount.zero(issuedToken)
//            // We can't pay back more than redeeming.
//            // Additionally, it doesn't make sense to run redeem and pay exact change.
//            require(inputSum > outSum) { "Change shouldn't exceed amount redeemed." }
//            // There can only be one issuer per group as the issuer is part of the token which is used to group states.
//            // If there are multiple issuers for the same tokens then there will be a group for each issued token. So,
//            // the line below should never fail on single().
//            val issuerKey: PublicKey = inputs.map { it.state.data }.map(ConfidentialToken::issuer).toSet().single().owningKey
//            val ownersKeys: List<PublicKey> = inputs.map { it.state.data.holder.owningKey }
//            val signers = redeemCommand.signers
//            require(issuerKey in signers) {
//                "The issuer must be the signing party when an amount of tokens are redeemed."
//            }
//            require(signers.containsAll(ownersKeys)) {
//                "Owners of redeemed states must be the signing parties."
//            }
//        }
    }
}

//package net.corda.ct
//
//import com.nhaarman.mockito_kotlin.doReturn
//import com.nhaarman.mockito_kotlin.mock
//import com.nhaarman.mockito_kotlin.whenever
//import com.r3.corda.lib.tokens.contracts.commands.IssueTokenCommand
//import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
//import com.r3.corda.lib.tokens.contracts.types.TokenType
//import com.r3.corda.lib.tokens.contracts.utilities.heldBy
//import com.r3.corda.lib.tokens.contracts.utilities.of
//import net.corda.core.contracts.Amount
//import net.corda.core.contracts.TypeOnlyCommandData
//import net.corda.core.identity.CordaX500Name
//import net.corda.core.node.NotaryInfo
//import net.corda.core.node.services.IdentityService
//import net.corda.testing.common.internal.testNetworkParameters
//import net.corda.testing.core.DUMMY_NOTARY_NAME
//import net.corda.testing.core.SerializationEnvironmentRule
//import net.corda.testing.core.TestIdentity
//import net.corda.testing.dsl.EnforceVerifyOrFail
//import net.corda.testing.dsl.TransactionDSL
//import net.corda.testing.dsl.TransactionDSLInterpreter
//import net.corda.testing.node.MockServices
//import net.corda.testing.node.transaction
//import org.junit.Rule
//import org.junit.Test
//import java.util.*
//
//class FungibleTokenTests {
//    private companion object {
//        val NOTARY = TestIdentity(DUMMY_NOTARY_NAME, 20)
//        val ISSUER = TestIdentity(CordaX500Name("ISSUER", "London", "GB"))
//        val ALICE = TestIdentity(CordaX500Name("ALICE", "London", "GB"))
//        val BOB = TestIdentity(CordaX500Name("BOB", "London", "GB"))
//        val CHARLIE = TestIdentity(CordaX500Name("CHARLIE", "London", "GB"))
//        val DAENERYS = TestIdentity(CordaX500Name("DAENERYS", "London", "GB"))
//    }
//
//    @Rule
//    @JvmField
//    private val testSerialization = SerializationEnvironmentRule()
//
//    private val aliceServices = MockServices(
//            cordappPackages = listOf("com.r3.corda.lib.tokens.contracts", "com.r3.corda.lib.tokens.money"),
//            initialIdentity = ALICE,
//            identityService = mock<IdentityService>().also {
//                doReturn(ALICE.party).whenever(it).partyFromKey(ALICE.publicKey)
//                doReturn(BOB.party).whenever(it).partyFromKey(BOB.publicKey)
//                doReturn(CHARLIE.party).whenever(it).partyFromKey(CHARLIE.publicKey)
//                doReturn(DAENERYS.party).whenever(it).partyFromKey(DAENERYS.publicKey)
//                doReturn(ISSUER.party).whenever(it).partyFromKey(ISSUER.publicKey)
//            },
//            networkParameters = testNetworkParameters(
//                    minimumPlatformVersion = 4,
//                    notaries = listOf(NotaryInfo(NOTARY.party, false))
//            )
//    )
//
//    private fun transaction(script: TransactionDSL<TransactionDSLInterpreter>.() -> EnforceVerifyOrFail) {
//        aliceServices.transaction(NOTARY.party, script)
//    }
//
//    private class WrongCommand : TypeOnlyCommandData()
//
//
//
//    @Test
//    fun `issue token tests`() {
//
//        val currency = Currency.getInstance("GBP")
//        val GBP = TokenType(currency.currencyCode, currency.defaultFractionDigits)
//        val issuedToken = IssuedTokenType(ISSUER.party, GBP)
//
//        transaction {
//            // Start with only one output.
//            output(ConfidentialTokenContract.contractId, 10 of issuedToken heldBy ALICE.party)
//            // No command fails.
//            tweak {
//                this `fails with` "A transaction must contain at least one command"
//            }
//            // Signed by a party other than the issuer.
//            tweak {
//                command(BOB.publicKey, IssueTokenCommand(issuedToken, listOf(0)))
//                this `fails with` "The issuer must be the signing party when an amount of tokens are issued."
//            }
//            // Non issuer signature present.
//            tweak {
//                command(listOf(BOB.publicKey, BOB.publicKey), IssueTokenCommand(issuedToken, listOf(0)))
//                this `fails with` "The issuer must be the signing party when an amount of tokens are issued."
//            }
//            // Non issuer signature present.
//            tweak {
//                command(listOf(ISSUER.publicKey, BOB.publicKey), IssueTokenCommand(issuedToken, listOf(0)))
//                verifies()
//            }
//            // With an incorrect command.
//            tweak {
//                command(BOB.publicKey, WrongCommand())
//                this `fails with` "There must be at least one token command in this transaction."
//            }
//            // With different command types for one group.
//            tweak {
//                command(ISSUER.publicKey, IssueTokenCommand(issuedToken, listOf(0)))
//                command(ISSUER.publicKey, MoveTokenCommand(issuedToken, listOf(0)))
//                verifies()
//            }
//            // Includes a group with no assigned command.
//            tweak {
//                output(FungibleTokenContract.contractId, 10.USD issuedBy ISSUER.party heldBy ALICE.party)
//                command(ISSUER.publicKey, IssueTokenCommand(issuedToken, listOf(0)))
//                this `fails with` "There is a token group with no assigned command!"
//            }
//            // With a zero amount in another group.
//            tweak {
//                val otherToken = USD issuedBy ISSUER.party
//                output(FungibleTokenContract.contractId, 0 of otherToken heldBy ALICE.party)
//                command(ISSUER.publicKey, IssueTokenCommand(issuedToken, listOf(0)))
//                command(ISSUER.publicKey, IssueTokenCommand(otherToken, listOf(1)))
//                this `fails with` "When issuing tokens an amount > ZERO must be issued."
//            }
//            // With some input states.
//            tweak {
//                input(FungibleTokenContract.contractId, 10 of issuedToken heldBy ALICE.party)
//                command(ISSUER.publicKey, IssueTokenCommand(issuedToken))
//                this `fails with` "There is a token group with no assigned command"
//            }
//            // Includes a zero output.
//            tweak {
//                output(FungibleTokenContract.contractId, 0 of issuedToken heldBy ALICE.party)
//                command(ISSUER.publicKey, IssueTokenCommand(issuedToken))
//                this `fails with` "There is a token group with no assigned command"
//            }
//            // Includes another token type and a matching command.
//            tweak {
//                val otherToken = USD issuedBy ISSUER.party
//                output(FungibleTokenContract.contractId, 10 of otherToken heldBy ALICE.party)
//                command(ISSUER.publicKey, IssueTokenCommand(issuedToken, listOf(0)))
//                command(ISSUER.publicKey, IssueTokenCommand(otherToken, listOf(1)))
//                verifies()
//            }
//            // Includes more output states of the same token type.
//            tweak {
//                output(FungibleTokenContract.contractId, 10 of issuedToken heldBy ALICE.party)
//                output(FungibleTokenContract.contractId, 100 of issuedToken heldBy ALICE.party)
//                output(FungibleTokenContract.contractId, 1000 of issuedToken heldBy ALICE.party)
//                command(ISSUER.publicKey, IssueTokenCommand(issuedToken, listOf(0, 1, 2, 3)))
//                verifies()
//            }
//            // Includes the same token issued by a different issuer.
//            // You wouldn't usually do this but it is possible.
//            tweak {
//                output(FungibleTokenContract.contractId, 1.GBP issuedBy BOB.party heldBy ALICE.party)
//                command(ISSUER.publicKey, IssueTokenCommand(issuedToken, listOf(0)))
//                command(BOB.publicKey, IssueTokenCommand(GBP issuedBy BOB.party, listOf(1)))
//                verifies()
//            }
//            // With the correct command and signed by the issuer.
//            tweak {
//                command(ISSUER.publicKey, IssueTokenCommand(issuedToken, listOf(0)))
//                verifies()
//            }
//        }
//    }
//
//    @Test
//    fun `move token tests`() {
//        val issuedToken = GBP issuedBy ISSUER.party
//        transaction {
//            // Start with a basic move which moves 10 tokens in entirety from ALICE to BOB.
//            input(FungibleTokenContract.contractId, 10 of issuedToken heldBy ALICE.party)
//            output(FungibleTokenContract.contractId, 10 of issuedToken heldBy BOB.party)
//            //move command with indicies
//            command(ALICE.publicKey, MoveTokenCommand(issuedToken, inputs = listOf(0), outputs = listOf(0)))
//
//            // Add the move command, signed by ALICE.
//            tweak {
//                verifies()
//            }
//
//            // Move coupled with an issue.
//            tweak {
//                output(FungibleTokenContract.contractId, 10.USD issuedBy BOB.party heldBy ALICE.party)
//                //the issue token is added after the move tokens, so it will have index(1)
//                command(BOB.publicKey, IssueTokenCommand(USD issuedBy BOB.party, outputs = listOf(1)))
//
//                verifies()
//            }
//
//            // Input missing.
//            tweak {
//                output(FungibleTokenContract.contractId, 10.USD issuedBy BOB.party heldBy BOB.party)
//                command(ALICE.publicKey, MoveTokenCommand(USD issuedBy BOB.party, outputs = listOf(1)))
//
//                this `fails with` "When moving tokens, there must be input states present."
//            }
//
//            // Output missing.
//            tweak {
//                input(FungibleTokenContract.contractId, 10.USD issuedBy BOB.party heldBy ALICE.party)
//                command(ALICE.publicKey, MoveTokenCommand(USD issuedBy BOB.party, inputs = listOf(1)))
//
//                this `fails with` "When moving tokens, there must be output states present."
//            }
//
//            // Inputs sum to zero.
//            tweak {
//                input(FungibleTokenContract.contractId, 0.USD issuedBy BOB.party heldBy ALICE.party)
//                input(FungibleTokenContract.contractId, 0.USD issuedBy BOB.party heldBy ALICE.party)
//                output(FungibleTokenContract.contractId, 10.USD issuedBy BOB.party heldBy BOB.party)
//                command(ALICE.publicKey, MoveTokenCommand(USD issuedBy BOB.party, inputs = listOf(1, 2), outputs = listOf(1)))
//                // Command for the move.
//                this `fails with` "In move groups there must be an amount of input tokens > ZERO."
//            }
//
//            // Outputs sum to zero.
//            tweak {
//                input(FungibleTokenContract.contractId, 10.USD issuedBy BOB.party heldBy ALICE.party)
//                output(FungibleTokenContract.contractId, 0.USD issuedBy BOB.party heldBy BOB.party)
//                output(FungibleTokenContract.contractId, 0.USD issuedBy BOB.party heldBy BOB.party)
//                command(ALICE.publicKey, MoveTokenCommand(USD issuedBy BOB.party, inputs = listOf(1), outputs = listOf(1, 2)))
//                // Command for the move.
//                this `fails with` "In move groups there must be an amount of output tokens > ZERO."
//            }
//
//            // Unbalanced move.
//            tweak {
//                input(FungibleTokenContract.contractId, 10.USD issuedBy BOB.party heldBy ALICE.party)
//                output(FungibleTokenContract.contractId, 11.USD issuedBy BOB.party heldBy BOB.party)
//                command(ALICE.publicKey, MoveTokenCommand(USD issuedBy BOB.party, inputs = listOf(1), outputs = listOf(1)))
//                // Command for the move.
//                this `fails with` "In move groups the amount of input tokens MUST EQUAL the amount of output tokens. " +
//                        "In other words, you cannot create or destroy value when moving tokens."
//            }
//
//            tweak {
//                input(FungibleTokenContract.contractId, 10.USD issuedBy BOB.party heldBy ALICE.party)
//                output(FungibleTokenContract.contractId, 10.USD issuedBy BOB.party heldBy BOB.party)
//                output(FungibleTokenContract.contractId, 0.USD issuedBy BOB.party heldBy BOB.party)
//                command(ALICE.publicKey, MoveTokenCommand(USD issuedBy BOB.party, inputs = listOf(1), outputs = listOf(1, 2)))
//                // Command for the move.
//                this `fails with` "You cannot create output token amounts with a ZERO amount."
//            }
//
//            // Two moves (two different groups).
//            tweak {
//                input(FungibleTokenContract.contractId, 10.USD issuedBy BOB.party heldBy ALICE.party)
//                output(FungibleTokenContract.contractId, 10.USD issuedBy BOB.party heldBy BOB.party)
//                command(ALICE.publicKey, MoveTokenCommand(USD issuedBy BOB.party, inputs = listOf(1), outputs = listOf(1)))
//                // Command for the move.
//                verifies()
//            }
//
//            // Two moves (one group).
//            tweak {
//                input(FungibleTokenContract.contractId, 20 of GBP issuedBy CHARLIE.party heldBy CHARLIE.party)
//                output(FungibleTokenContract.contractId, 20 of GBP issuedBy CHARLIE.party heldBy DAENERYS.party)
//
//                input(FungibleTokenContract.contractId, 20 of RUB issuedBy CHARLIE.party heldBy CHARLIE.party)
//                output(FungibleTokenContract.contractId, 10 of RUB issuedBy CHARLIE.party heldBy CHARLIE.party)
//                output(FungibleTokenContract.contractId, 10 of RUB issuedBy CHARLIE.party heldBy CHARLIE.party)
//
//                attachment(RUB.importAttachment(aliceServices.attachments))
//
//                command(CHARLIE.publicKey, MoveTokenCommand(GBP issuedBy CHARLIE.party, inputs = listOf(1), outputs = listOf(1)))
//                command(CHARLIE.publicKey, MoveTokenCommand(RUB issuedBy CHARLIE.party, inputs = listOf(2), outputs = listOf(2, 3)))
//                verifies()
//            }
//
//            // Wrong public key.
//            tweak {
//                attachment(RUB.importAttachment(aliceServices.attachments))
//                input(FungibleTokenContract.contractId, 20 of RUB issuedBy CHARLIE.party heldBy CHARLIE.party)
//                output(FungibleTokenContract.contractId, 20 of RUB issuedBy CHARLIE.party heldBy DAENERYS.party)
//                command(BOB.publicKey, MoveTokenCommand(RUB issuedBy CHARLIE.party, inputs = listOf(1), outputs = listOf(1)))
//                this `fails with` "Required signers does not contain all the current owners of the tokens being moved"
//            }
//        }
//    }
//
//
//}

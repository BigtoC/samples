package net.corda.ct.contract

import com.r3.corda.lib.tokens.contracts.internal.schemas.FungibleTokenSchemaV1
import com.r3.corda.lib.tokens.contracts.states.AbstractToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.getAttachmentIdForGenericParam
import com.r3.corda.lib.tokens.contracts.utilities.holderString
import net.corda.core.contracts.BelongsToContract
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

@BelongsToContract(ConfidentialTokenContract::class)
open class ConfidentialToken(
        val amount: ConfidentialAmount<IssuedTokenType>,
        override val holder: AbstractParty,
        override val tokenTypeJarHash: SecureHash? = amount.token.tokenType.getAttachmentIdForGenericParam()
) : AbstractToken, QueryableState {
    override val tokenType: TokenType get() = amount.token.tokenType
    override val issuedTokenType: IssuedTokenType get() = amount.token
    override val issuer: Party get() = amount.token.issuer

    override fun toString(): String = "$amount held by $holderString"

    override fun withNewHolder(newHolder: AbstractParty): ConfidentialToken {
        return ConfidentialToken(amount = amount, holder = newHolder, tokenTypeJarHash = tokenTypeJarHash)
    }

    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is ConfidentialTokenSchemaV1 -> ConfidentialTokenSchemaV1.PersistentConfidentialToken(
                issuer = amount.token.issuer,
                holder = holder,
                amount = null,
                secret = null,
                tokenClass = amount.token.tokenType.tokenClass,
                tokenIdentifier = amount.token.tokenType.tokenIdentifier
        )
        else -> throw IllegalArgumentException("Unrecognised schema $schema")
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(ConfidentialTokenSchemaV1)
}



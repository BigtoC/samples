package net.corda.ct

import com.r3.corda.lib.tokens.contracts.states.AbstractToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.getAttachmentIdForGenericParam
import com.r3.corda.lib.tokens.contracts.utilities.holderString
import net.corda.core.contracts.BelongsToContract
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(ConfidentialTokenContract::class)
open class ConfidentialToken(
        val amount: ConfidentialAmount<IssuedTokenType>,
        override val holder: AbstractParty,
        override val tokenTypeJarHash: SecureHash? = amount.token.tokenType.getAttachmentIdForGenericParam()
) : AbstractToken {

    override val tokenType: TokenType get() = amount.token.tokenType

    override val issuedTokenType: IssuedTokenType get() = amount.token

    override val issuer: Party get() = amount.token.issuer

    override fun toString(): String = "$amount held by $holderString"

    override fun withNewHolder(newHolder: AbstractParty): ConfidentialToken {
        return ConfidentialToken(amount = amount, holder = newHolder, tokenTypeJarHash = tokenTypeJarHash)
    }
}


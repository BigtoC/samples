package net.corda.ct.contract

import com.r3.corda.lib.tokens.contracts.internal.schemas.TokenClassConverter
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for IOUState.
 */
object ConfidentialTokenSchema

/**
 * An IOUState schema.
 */
object ConfidentialTokenSchemaV1 : MappedSchema(
        schemaFamily = ConfidentialTokenSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentConfidentialToken::class.java)) {
    @Entity
    @Table(name = "confidential_tokens")
    class PersistentConfidentialToken(
            @Column(name = "issuer", nullable = false)
            var issuer: Party? = null,

            @Column(name = "holder")
            var holder: AbstractParty? = null,

            /**
             * The clear-text amount, if revealed.
             * The [amount] field has to be updated after the state is recorded,
             * as it's not stored in the transaction.
             */
            @Column(name = "amount", nullable = true)
            var amount: Long? = null,

            /**
             * The secret to verify the amount commitment.
             * Same as [amount], the secret has to be updated after the state is recorded,
             * as it's not stored on the transaction.
             */
            @Column(name = "secret", nullable = true)
            var secret: String? = null, // BigInteger

            @Column(name = "token_class", nullable = false)
            @Convert(converter = TokenClassConverter::class)
            var tokenClass: Class<*>? = null,


            @Column(name = "token_identifier", nullable = true)
            var tokenIdentifier: String? = null
    ) : PersistentState()
}
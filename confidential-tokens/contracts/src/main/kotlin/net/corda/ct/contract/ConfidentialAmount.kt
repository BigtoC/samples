package net.corda.ct.contract


import net.corda.core.contracts.Amount
import net.corda.core.serialization.CordaSerializable
import net.corda.ct.crypto.PedersenCommitment
import java.math.BigDecimal
import java.math.BigInteger

// TODO: Add a range proof to prove the quantity is non-negative.

@CordaSerializable
data class ConfidentialAmount<T : Any>(
        val hiddenQuantity: PedersenCommitment,
        val displayTokenSize: BigDecimal,
        val token: T
) {
    companion object {
        fun <T : Any> generate(amount: Amount<T>, secret: BigInteger): ConfidentialAmount<T> {
            val hiddenQuantity = PedersenCommitment.generate(BigInteger.valueOf(amount.quantity), secret)
            return ConfidentialAmount(hiddenQuantity, amount.displayTokenSize, amount.token)
        }
    }

    operator fun plus(other: ConfidentialAmount<T>): ConfidentialAmount<T> {
        checkToken(other)
        return ConfidentialAmount(hiddenQuantity + other.hiddenQuantity, displayTokenSize, token)
    }

    private fun checkToken(other: ConfidentialAmount<T>) {
        require(other.token == token) { "Token mismatch: ${other.token} vs $token" }
        require(other.displayTokenSize == displayTokenSize) { "Token size mismatch: ${other.displayTokenSize} vs $displayTokenSize" }
    }
}

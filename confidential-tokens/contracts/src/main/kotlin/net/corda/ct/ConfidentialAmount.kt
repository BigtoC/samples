package net.corda.ct

import java.math.BigDecimal

data class ConfidentialAmount<T : Any>(
        val quantity: Long,
        val displayTokenSize: BigDecimal,
        val token: T) {
    companion object {
    }

    init {
        require(quantity >= 0) { "Negative amounts are not allowed: $quantity" }
    }
}

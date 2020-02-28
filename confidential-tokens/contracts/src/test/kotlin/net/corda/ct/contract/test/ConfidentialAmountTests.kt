package net.corda.ct.contract.test

import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.amount
import net.corda.ct.contract.ConfidentialAmount
import net.corda.testing.core.DUMMY_BANK_A_NAME
import net.corda.testing.core.TestIdentity
import org.junit.Test
import java.math.BigInteger
import java.util.*
import kotlin.test.assertTrue

class ConfidentialAmountTests {
    private fun getTokenType(): IssuedTokenType {
        val currency = Currency.getInstance("GBP")
        val GBP = TokenType(currency.currencyCode, currency.defaultFractionDigits)
        return IssuedTokenType(TestIdentity(DUMMY_BANK_A_NAME).party, GBP)
    }

    @Test
    fun `test confidential amount determinism`() {
        val moveQuantity = 100L
        val moveAmount = amount(moveQuantity, getTokenType())
        val moveSecret = BigInteger.valueOf(10)

        val confidentialAmountA = ConfidentialAmount.generate(moveAmount, moveSecret)
        val confidentialAmountB= ConfidentialAmount.generate(moveAmount, moveSecret)

        assertTrue { confidentialAmountA == confidentialAmountB }
        assertTrue { confidentialAmountA.hiddenQuantity == confidentialAmountB.hiddenQuantity }
    }
}
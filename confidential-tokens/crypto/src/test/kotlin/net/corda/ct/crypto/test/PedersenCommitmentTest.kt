package net.corda.ct.crypto.test

import net.corda.ct.crypto.PedersenCommitment
import net.corda.ct.crypto2.mapToPoint
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.junit.Test
import java.math.BigInteger
import kotlin.test.assertTrue

class PedersenCommitmentTest {
    @Test
    fun pedersenCommitmentTest() {
        val curveParameters = CustomNamedCurves.getByName("Curve25519")
        val H = curveParameters.curve.mapToPoint(curveParameters.g)

        val valueA = BigInteger.valueOf(5)
        val rA = BigInteger.valueOf(12)
        val pedersenCommitmentA = PedersenCommitment.generate(valueA, rA)

        val valueB = BigInteger.valueOf(10)
        val rB = BigInteger.valueOf(49)
        val pedersenCommitmentB = PedersenCommitment.generate(valueB, rB)

        val valueSum = valueA + valueB
        val rSum = rA + rB
        val pedersenCommitmentSum = PedersenCommitment.generate(valueSum, rSum)

        val pedersenCommitmentAB = pedersenCommitmentA + pedersenCommitmentB
        assertTrue { pedersenCommitmentSum == pedersenCommitmentAB }
    }
}
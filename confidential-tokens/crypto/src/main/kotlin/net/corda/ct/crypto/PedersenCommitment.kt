package net.corda.ct.crypto

import net.corda.ct.crypto2.mapToPoint
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger
import java.security.SecureRandom

data class PedersenCommitment(val commitmentEcPoint: ECPoint) {
    companion object {
        val curveParameters: X9ECParameters = CustomNamedCurves.getByName("Curve25519")

        private val H = curveParameters.curve.mapToPoint(curveParameters.g)

        /**
         * Generates a Pedersen commitment for the given [value],
         * using the specified blinding factor [r].
         */
        fun generate(value: BigInteger, r: BigInteger): PedersenCommitment {
            val commitmentEcPoint = curveParameters.g.multiply(r).add(H.multiply(value))
            return PedersenCommitment(commitmentEcPoint)
        }

        private val random = SecureRandom()

        /** Generate a secret to be used as the blinding factor for a Pederseon commitment. */
        fun generateSecret(): BigInteger = BigInteger.valueOf(random.nextLong()) % curveParameters.n
    }

    fun verify(value: BigInteger, r: BigInteger) {
        val commitment = generate(value, r)
        require(this == commitment) {
            "The specified value and blinding factor does not match this commitment."
        }
    }

    operator fun plus(other: PedersenCommitment) = PedersenCommitment(commitmentEcPoint.add(other.commitmentEcPoint))

    override fun equals(other: Any?): Boolean {
        val otherPoint = (other as? PedersenCommitment)?.commitmentEcPoint
        return commitmentEcPoint.equals(otherPoint)
    }

    override fun hashCode(): Int {
        return commitmentEcPoint.hashCode()
    }
}
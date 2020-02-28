package net.corda.ct.crypto

import net.corda.ct.crypto2.mapToPoint
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger
import java.security.SecureRandom

object CryptoParameters {
    val curve: X9ECParameters = CustomNamedCurves.getByName("Curve25519")
    val g: ECPoint = curve.g
    val n: BigInteger get() = curve.n
    val H = curve.curve.mapToPoint(curve.g)

    private val random = SecureRandom()

    /** Generate a secret to be used as the blinding factor for a Pederseon commitment. */
    fun generateSecret(): BigInteger = BigInteger.valueOf(random.nextLong()) % n
}

data class PedersenCommitment(val commitmentEcPoint: ECPoint) {
    companion object {
        /**
         * Generates a Pedersen commitment for the given [value],
         * using the specified blinding factor [r].
         */
        fun generate(value: BigInteger, r: BigInteger): PedersenCommitment {
            val g = CryptoParameters.curve.g
            val H = CryptoParameters.H
            val commitmentEcPoint = g.multiply(r).add(H.multiply(value))
            return PedersenCommitment(commitmentEcPoint)
        }
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

class BorromeanRangeProof private constructor(
        val commitment: PedersenCommitment,
        val signature: AOSSignature,
        val rangeKeys: List<ECPoint>) : RangeProof {
    companion object {
        private const val MAX_VALUE = 256

        fun generate(value: BigInteger): BorromeanRangeProof {
            val secret = CryptoParameters.generateSecret()
            val pedersenCommitment = PedersenCommitment.generate(value, secret)
            val pedersenPoint = pedersenCommitment.commitmentEcPoint
            val rangeKeys: List<ECPoint> = createAllRingPublicKeyPoints(pedersenCommitment.commitmentEcPoint)
            val aosSig = AOS.sign(
                    CryptoParameters.n,
                    CryptoParameters.g,
                    pedersenPoint.getEncoded(true),
                    rangeKeys,
                    value.toInt(),
                    secret
            )
            return BorromeanRangeProof(pedersenCommitment, aosSig, rangeKeys)
        }

        private fun createAllRingPublicKeyPoints(commitmentPoint: ECPoint): List<ECPoint> {
            val listOfKeys = mutableListOf(commitmentPoint)
            for (i in 1 until MAX_VALUE) {
                listOfKeys.add(commitmentPoint.subtract(CryptoParameters.H.multiply(BigInteger.valueOf(i.toLong()))))
            }
            return listOfKeys
        }
    }

    override fun verify() {
        AOS.verify(CryptoParameters.g, commitment.commitmentEcPoint.getEncoded(true), rangeKeys, signature)
    }
}
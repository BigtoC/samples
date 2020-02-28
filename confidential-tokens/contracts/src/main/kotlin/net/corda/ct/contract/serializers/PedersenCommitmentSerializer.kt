package net.corda.ct.contract.serializers

import net.corda.core.serialization.SerializationCustomSerializer
import net.corda.core.utilities.OpaqueBytes
import net.corda.ct.crypto.PedersenCommitment

class PedersenCommitmentSerializer : SerializationCustomSerializer<PedersenCommitment, PedersenCommitmentProxy> {
    override fun toProxy(obj: PedersenCommitment) = PedersenCommitmentProxy(OpaqueBytes(obj.commitmentEcPoint.getEncoded(true)))
    override fun fromProxy(proxy: PedersenCommitmentProxy): PedersenCommitment {
        val curve = PedersenCommitment.curveParameters.curve
        val ecPoint = curve.decodePoint(proxy.encodedEcPoint.bytes)
        return PedersenCommitment(ecPoint)
    }
}

data class PedersenCommitmentProxy(val encodedEcPoint: OpaqueBytes)


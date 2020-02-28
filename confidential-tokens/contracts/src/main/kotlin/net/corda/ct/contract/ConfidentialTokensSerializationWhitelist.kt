package net.corda.ct.contract

import net.corda.core.serialization.SerializationWhitelist
import net.corda.ct.crypto.PedersenCommitment

class ConfidentialTokensSerializationWhitelist : SerializationWhitelist {
    override val whitelist: List<Class<*>> = listOf(
            PedersenCommitment::class.java
    )
}
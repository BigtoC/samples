//package net.corda.ct.contract.test
//
//import edu.stanford.cs.crypto.efficientct.GeneratorParams
//import edu.stanford.cs.crypto.efficientct.algebra.*
//import edu.stanford.cs.crypto.efficientct.commitments.PeddersenCommitment
//import edu.stanford.cs.crypto.efficientct.rangeproof.RangeProofProver
//import edu.stanford.cs.crypto.efficientct.rangeproof.RangeProofVerifier
//import edu.stanford.cs.crypto.efficientct.util.ProofUtils
//import org.junit.Test
//import java.math.BigInteger
//
//
//class RangeProofTest {
//    @Test
//    fun testCompletness() {
//        test(BN128Group())
//    }
//
//
//    private fun <T: GroupElement<T>> test(curve: Group<T>) {
//        val rangeLimit = 256
//
//
//        val number = BigInteger("-1321314563231323465")
//        val randomness = ProofUtils.randomNumber()
//
//        val parameters = GeneratorParams.generateParams(256, curve)
//
//        val v = parameters.base.commit(number, randomness)
//        val witness = PeddersenCommitment(parameters.base, number, randomness)
//        val proof = RangeProofProver<T>().generateProof(parameters, v, witness)
//
//        val verifier = RangeProofVerifier<T>()
//        verifier.verify(parameters, v, proof)
//    }
//}
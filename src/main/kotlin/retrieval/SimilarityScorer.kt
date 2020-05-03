package retrieval

import org.apache.commons.math3.linear.RealVector

class SimilarityScorer(private val scoreFunctionName: String = "") {

    fun score(v1: RealVector, v2: RealVector): Double {
        return when(scoreFunctionName) {
            "dot"       -> dotSim(v1, v2)
            "cosine"    -> cosineSim(v1, v2)
            else        -> cosineSim(v1, v2)
        }
    }

    private fun cosineSim(v1: RealVector, v2: RealVector): Double {
        println("Computing cosine()")
        return v1.unitVector().dotProduct(v2.unitVector())
    }

    private fun dotSim(v1: RealVector, v2: RealVector): Double {
        println("Computing dot()")
        return v1.dotProduct(v2)
    }
}
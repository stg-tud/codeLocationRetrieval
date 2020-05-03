package retrieval

import org.apache.commons.math3.linear.RealVector

class SimilarityScorer(private val scoreStrategy: String = "") {

    fun score(v1: RealVector, v2: RealVector): Double {
        return when(scoreStrategy) {
            "dot"       -> dotSim(v1, v2)
            "cosine"    -> cosineSim(v1, v2)
            else        -> cosineSim(v1, v2)
        }
    }

    private fun cosineSim(v1: RealVector, v2: RealVector): Double {
        return v1.unitVector().dotProduct(v2.unitVector())
    }

    private fun dotSim(v1: RealVector, v2: RealVector): Double {
        return v1.dotProduct(v2)
    }
}
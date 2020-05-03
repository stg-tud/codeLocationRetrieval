import org.apache.commons.math3.linear.MatrixUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import retrieval.SimilarityScorer

class ScorerTest {

    @Test
    fun testDotSimilarity() {
        // Given two vectors of equal dimensions (e.g. representations of documents/queries)
        val v1 = MatrixUtils.createRealVector(doubleArrayOf(1.0, 3.0, 1.0, 4.0, 5.0))
        val v2 = MatrixUtils.createRealVector(doubleArrayOf(2.0, 2.0, 0.0, 2.0, 0.0))

        // When we score them with "dot" similarity
        val dotScorer = SimilarityScorer("dot")
        val actualScore = dotScorer.score(v1, v2)

        // Then the actual score will be the dot product of the two vectors
        assertThat(actualScore).isCloseTo(16.0, Offset.offset(0.01))
    }

    @Test
    fun testCosineSimilarity() {
        // Given two vectors of equal dimensions (e.g. representations of documents/queries)
        val v1 = MatrixUtils.createRealVector(doubleArrayOf(1.0, 3.0, 1.0, 4.0, 5.0))
        val v2 = MatrixUtils.createRealVector(doubleArrayOf(2.0, 2.0, 0.0, 2.0, 0.0))

        // When we score them with "cosine" similarity
        val cosineScorer = SimilarityScorer("cosine")
        val actualScore = cosineScorer.score(v1, v2)

        // Then the actual score will be the cosine of the angle between the two vectors
        assertThat(actualScore).isCloseTo(0.6405, Offset.offset(0.0001))
    }
}
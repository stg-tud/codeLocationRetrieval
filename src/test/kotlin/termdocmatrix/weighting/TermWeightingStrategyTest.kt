package termdocmatrix.weighting

import io.mockk.every
import io.mockk.mockk
import termdocmatrix.TermDocumentMatrix
import org.assertj.core.api.AbstractDoubleArrayAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.withPrecision
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import preprocessor.Document

class TermWeightingStrategyTest {

    private lateinit var matrixToBeWeighted: TermDocumentMatrix

    @BeforeEach
    fun setupMatrix() {
        val doc1 = mockk<Document>()
        val doc2 = mockk<Document>()
        val doc3 = mockk<Document>()

//        every { doc1.terms } returns listOf("t1", "t1", "t1", "t2", "t2", "t5", "t5", "t5", "t5")
//        every { doc2.terms } returns listOf("t1", "t2", "t2", "t3", "t3", "t3", "t4", "t4", "t5")
//        every { doc3.terms } returns listOf("t1", "t3", "t4", "t4", "t4", "t4", "t4", "t4", "t4")
//
//        matrixToBeWeighted = TermDocumentMatrix(
//            setOf("t1", "t2", "t3", "t4", "t5"),
//            listOf(doc1, doc2, doc3)
//        )
    }

    @Test
    fun testTermFrequencyWeighting() {
        // Given a term-document matrix

        // When applying the term-frequency weighting scheme
        val actualMatrix = TermFrequencyWeighting().weightEntries(matrixToBeWeighted)

        // Then there should be no change
        assertThat(actualMatrix).isEqualTo(matrixToBeWeighted)

        // TODO: verify that methods were called (-> mocking)? Or leave as is (-> stubbing)?
    }

    @Test
    fun testLocalBinaryFrequencyWeighting() {
        // Given a term-document matrix

        // When applying the local-binary weighting scheme
        val actualMatrix = LocalBinaryWeighting().weightEntries(matrixToBeWeighted)

        // Then all entries greater than 0 should now equal 1
        //                                                       d1   d2   d3
        assertThat(actualMatrix.data[0]).isEqualTo(doubleArrayOf(1.0, 1.0, 1.0))    // t1 occurs in d1, d2, and d3
        assertThat(actualMatrix.data[1]).isEqualTo(doubleArrayOf(1.0, 1.0, 0.0))    // t2 occurs in d1 and d2
        assertThat(actualMatrix.data[2]).isEqualTo(doubleArrayOf(0.0, 1.0, 1.0))    // t3 ...
        assertThat(actualMatrix.data[3]).isEqualTo(doubleArrayOf(0.0, 1.0, 1.0))    // t4 ...
        assertThat(actualMatrix.data[4]).isEqualTo(doubleArrayOf(1.0, 1.0, 0.0))    // t5 ...
    }

    @Test
    fun testTfIdfWeighting() {
        // Given a term-document matrix
        /*
         *      d1  d2  d3
         * t1   3   1   1
         * t2   2   2   0
         * t3   0   3   1
         * t4   0   2   7
         * t5   4   1   0
         */

        // When applying TF-IDF weighting (TFs being log-normalized!)
        val actualMatrix = TfIdfWeighting().weightEntries(matrixToBeWeighted)

        // Then
        actualMatrix.apply {
            //                                        d1     d2     d3
            assertThat(data[0]).isCloseTo(0.000, 0.000, 0.000)   // t1
            assertThat(data[1]).isCloseTo(0.084, 0.084, 0.000)   // t2
            assertThat(data[2]).isCloseTo(0.000, 0.106, 0.053)   // t3
            assertThat(data[3]).isCloseTo(0.000, 0.084, 0.159)   // t4
            assertThat(data[4]).isCloseTo(0.123, 0.053, 0.000)   // t5
        }
    }

    @Test
    fun testLogEntropyWeighting() {
        // Given term-document matrix

            /* Assumed TDM
            *
            *      d1  d2  d3
            * t1   3   1   1
            * t2   2   2   0
            * t3   0   3   1
            * t4   0   2   7
            * t5   4   1   0
            */
            /* CFs
             *
             * t1: 5
             * t2: 4
             * t3: 4
             * t4: 9
             * t5: 5
             */
            /* LOGs
             *
             * t1: 0.602, 0.301, 0.301
             * t2: 0.477, 0.477, 0.000
             * t3: 0.000, 0.602, 0.301
             * t4: 0.000, 0.477, 0.903
             * t5: 0.699, 0.301, 0.000
             */
            /* ENTROPYs
             *
             * t1: 0.135
             * t2: 0.369
             * t3: 0.488
             * t4: 0.518
             * t5: 0.545
             */

        // When applying Log-Entropy weighting
        val actualMatrix = LogEntropyWeighting().weightEntries(matrixToBeWeighted)

        // Then
        actualMatrix.apply {
            //
            assertThat(data[0]).isCloseTo(0.081, 0.040, 0.040)
            assertThat(data[1]).isCloseTo(0.176, 0.176, 0.000)
            assertThat(data[2]).isCloseTo(0.000, 0.294, 0.147)
            assertThat(data[3]).isCloseTo(0.000, 0.247, 0.468)
            assertThat(data[4]).isCloseTo(0.381, 0.164, 0.000)
        }
    }
}

private fun AbstractDoubleArrayAssert<*>.isCloseTo(vararg expected: Double) {
    this.containsSequence(expected, withPrecision(0.001))
}
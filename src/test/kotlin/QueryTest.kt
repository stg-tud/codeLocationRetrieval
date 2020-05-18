import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import retrieval.Query
import termdocmatrix.TermDocumentMatrix

class QueryTest {

    @MockK
    private lateinit var tdmMock: TermDocumentMatrix

    @Nested inner class NormalizationTests {

        @BeforeEach
        fun setUpMockForNormalizationTests() {
            tdmMock = mockk()
            every { tdmMock.numOfTerms } returns 0
            every { tdmMock.terms } returns HashSet()
        }

        @Test
        fun testQueryNormalization_simple() {
            // Given a user input for the query with simple words
            val inputQuery = "user query"

            // When processing the input
            val actualQueryTerms = Query(inputQuery, tdmMock).normalizedTerms

            // Then
            assertThat(actualQueryTerms).isEqualTo(listOf("user", "query"))
        }

        @Test
        fun testQueryNormalization_mixedCase() {
            // Given a query that contains mixed case, e.g. camel or pascal case
            val inputQuery = "camelCase pascalCase"

            // When processing the input
            val actualQueryTerms = Query(inputQuery, tdmMock).normalizedTerms

            // Then
            assertThat(actualQueryTerms).containsExactlyInAnyOrder(
                "camelcase", "camel", "case",
                "pascalcase", "pascal", "case")
        }

        @Test
        fun testQueryNormalization_underscores() {
            // Given
            val inputQuery = "_at_start in_the_middle at_end_"

            // When
            val actualQueryTerms = Query(inputQuery, tdmMock).normalizedTerms

            // Then
            assertThat(actualQueryTerms).containsExactlyInAnyOrder(
                "_at_start", "at", "start",
                "in_the_middle", "in", "the", "middle",
                "at_end_", "at", "end")
        }

        @Test
        fun testQueryNormalization_mixedCaseAndUnderscores() {
            // Given
            val inputQuery = "normal Title mixedCase MixedCase underScore_with_camelCase BIGCase CaseBIG ALL_UPPER SUPPER"

            // When
            val actualQueryTerms = Query(inputQuery, tdmMock).normalizedTerms

            // Then
            assertThat(actualQueryTerms).containsExactlyInAnyOrder(
                "normal",
                "title",
                "mixedcase", "mixed", "case",
                "mixedcase", "mixed", "case",
                "underscore_with_camelcase", "under", "score", "with", "camel", "case",
                "bigcase", "big", "case",
                "casebig", "case", "big",
                "all_upper", "all", "upper",
                "supper")
        }

        @Test
        fun testQueryNormalization_punctuation() {
            // Given
            val inputQuery = "'Query-normalization' (or so?) has to work \"100%\" of the time!!"

            // When
            val actualQueryTerms = Query(inputQuery, tdmMock).normalizedTerms

            assertThat(actualQueryTerms).containsExactlyInAnyOrder(
                "query", "normalization", "or", "so", "has", "to", "work", "100", "of", "the", "time")
        }
    }

    @Nested inner class VectorConstructionTests {

        /*
         * Actual values may depend on term weighting, so try to prefer tests that test for greater/smaller than,
         * rather than trying to verify precise numbers
         *
         * ... for now, at least
         */

        @BeforeEach
        fun setUpMockForVectorConstructionTests() {
            tdmMock = mockk()
//            every { tdmMock.terms } returns setOf("vector", "construction", "tests", "rock")
            every { tdmMock.numOfTerms } returns tdmMock.terms.size
        }

        @Test
        fun testVectorConstruction_simple() {
            // Given
            val inputQuery = "vector construction"

            // When
            val actualVector = Query(inputQuery, tdmMock).queryVector

            // Then
            assertThat(actualVector.getEntry(0)).isGreaterThan(0.0)
            assertThat(actualVector.getEntry(1)).isGreaterThan(0.0)
            assertThat(actualVector.getEntry(2)).isEqualTo(0.0)
            assertThat(actualVector.getEntry(3)).isEqualTo(0.0)
        }

        @Test
        fun testVectorConstruction_noIndexedTerms() {
            // Given
            val inputQuery = "This is a query that contains no terms that are part of the index (i.e. not in the TDM)"

            // When
            val actualVector = Query(inputQuery, tdmMock).queryVector

            // Then
            assertThat(actualVector.getEntry(0)).isEqualTo(0.0)
            assertThat(actualVector.getEntry(1)).isEqualTo(0.0)
            assertThat(actualVector.getEntry(2)).isEqualTo(0.0)
            assertThat(actualVector.getEntry(3)).isEqualTo(0.0)
        }

        @Test
        fun testVectorConstruction_mixedCaseAndUnderscores() {
            // Given
            val inputQuery = "vectorConstruction_TESTS_Rock"

            // When
            val actualVector = Query(inputQuery, tdmMock).queryVector

            // Then
            assertThat(actualVector.getEntry(0)).isGreaterThan(0.0)
            assertThat(actualVector.getEntry(1)).isGreaterThan(0.0)
            assertThat(actualVector.getEntry(2)).isGreaterThan(0.0)
            assertThat(actualVector.getEntry(3)).isGreaterThan(0.0)
        }

        @Test
        fun testVectorConstruction_punctuation() {
            // Given
            val inputQuery = "\"Vector\"-'construction'. (?Tests!, %rock%)."

            // When
            val actualVector = Query(inputQuery, tdmMock).queryVector

            // Then
            assertThat(actualVector.getEntry(0)).isGreaterThan(0.0)
            assertThat(actualVector.getEntry(1)).isGreaterThan(0.0)
            assertThat(actualVector.getEntry(2)).isGreaterThan(0.0)
            assertThat(actualVector.getEntry(3)).isGreaterThan(0.0)
        }
    }

    @Nested inner class IndexedTermsTests {

        @BeforeEach
        fun setUpMockForIndexedTermsTests() {
            tdmMock = mockk()
            every { tdmMock.terms.map { it.term }.toSet() } returns setOf("indexed", "terms", "tests", "rock")
            every { tdmMock.numOfTerms } returns tdmMock.terms.size
        }

        @Test
        fun testIndexedTerms_simple() {
            // Given
            val inputQuery = "indexed terms tests rock"

            // When
            val actualIndexedTerms = Query(inputQuery, tdmMock).indexedTerms

            // Then
            assertThat(actualIndexedTerms).isEqualTo(setOf("indexed", "terms", "tests", "rock"))
        }

        @Test
        fun testIndexedTerms_noIndexedTerms() {
            // Given
            val inputQuery = "A query that consists only of words not contained in the term-document matrix"

            // When
            val actualIndexedTerms = Query(inputQuery, tdmMock).indexedTerms

            // Then
            assertThat(actualIndexedTerms).isEmpty()
        }

        @Test
        fun testIndexedTerms_afterNormalization() {
            // Given
            val inputQuery = "indexedTerms_TESTS_Rock"

            // When
            val actualIndexedTerms = Query(inputQuery, tdmMock).indexedTerms

            // Then
            assertThat(actualIndexedTerms).isEqualTo(setOf("indexed", "terms", "tests", "rock"))
        }

        @Test
        fun testIndexedTerms_mixed() {
            // Given
            val inputQuery = "some terms are indexed, others are not"

            // When
            val actualIndexedTerms = Query(inputQuery, tdmMock).indexedTerms

            // Then
            assertThat(actualIndexedTerms).isEqualTo(setOf("indexed", "terms"))
        }
    }
}
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrieval.Query
import termdocmatrix.TermDocumentMatrix

class QueryTest {

    // TODO: for now mock since we only test normalization; but may have to adapt later
    @MockK
    private lateinit var tdmMock: TermDocumentMatrix

    @BeforeEach
    fun setUpMock() {
        tdmMock = mockk()

        every { tdmMock.numOfTerms } returns 0
        every { tdmMock.terms } returns emptySet()
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
    fun testQueryNormalization_all() {
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
}
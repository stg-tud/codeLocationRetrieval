
import org.assertj.core.api.Assertions
//import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import preprocessor.Preprocessor

class PreprocessorTest {
    private val preprocessor = Preprocessor()

    @Test
    fun testIdModifier() {
        // no changes
        Assertions.assertThat(preprocessor.getModifiedIdentifier("lowercase")).isNull()
        Assertions.assertThat(preprocessor.getModifiedIdentifier("UPPERCASE")).isNull()

        // camel case
        Assertions.assertThat(preprocessor.getModifiedIdentifier("URLLocation"))
            .isEqualTo("url location")

        // underscore
        Assertions.assertThat(preprocessor.getModifiedIdentifier("underscore_test"))
            .isEqualTo("underscore test")

        // mixed
        Assertions.assertThat(preprocessor.getModifiedIdentifier("someMixed_TestCase"))
            .isEqualTo("some mixed test case")

        // edge-cases
            // range-limit
        Assertions.assertThat(preprocessor.getModifiedIdentifier("URILo"))
            .isEqualTo("uri lo")

            // underscore at the beginning
        Assertions.assertThat(preprocessor.getModifiedIdentifier("_underscore"))
            .isEqualTo("underscore")

            // underscore at the end
        Assertions.assertThat(preprocessor.getModifiedIdentifier("underscore_"))
            .isEqualTo("underscore")

            // more than one underscore
        Assertions.assertThat(preprocessor.getModifiedIdentifier("_under_score_"))
            .isEqualTo("under score")
    }
}
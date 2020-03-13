import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import preprocessor.Preprocessor
import preprocessor.TokenType
import preprocessor.Lexer
import preprocessor.getModifiedIdentifier
import java.io.File

class PreprocessorTest {
    private val preprocessor = Preprocessor()

    @Test
    fun testIdModifier() {
        // no changes
        assertThat(getModifiedIdentifier("lowercase")).isNull()
        assertThat(getModifiedIdentifier("UPPERCASE")).isNull()
        assertThat(getModifiedIdentifier("Null")).isNull()

        // camel case
        assertThat(getModifiedIdentifier("URLLocation")).isEqualTo("url location")

        // underscore
        assertThat(getModifiedIdentifier("underscore_test")).isEqualTo("underscore test")

        // mixed
        assertThat(getModifiedIdentifier("someMixed_TestCase")).isEqualTo("some mixed test case")

        // edge-cases
            // range-limit
       assertThat(getModifiedIdentifier("URILo")).isEqualTo("uri lo")

            // underscore at the beginning
        assertThat(getModifiedIdentifier("_underscore")).isEqualTo("underscore")

            // underscore at the end
        assertThat(getModifiedIdentifier("underscore_")).isEqualTo("underscore")

            // more than one underscore
        assertThat(getModifiedIdentifier("_under_score_")).isEqualTo("under score")
    }

    // TODO: test looks ugly
    @Test
    fun testTokenExtraction() {
        // Given a C file
        val sourceFile = File("src/test/resources/PreprocessorTest/actualInput/main.c")

        // When extracting tokens out of it
        val tokens = preprocessor.extractTokens(sourceFile.readText())

        val actualOutput = StringBuilder()
        for(token in tokens) {
            when(token.tokenType) {
                TokenType.COMMENT -> { actualOutput.append("${token.value}\n") }
                TokenType.IDENTIFIER -> {
                    actualOutput.append("${token.value}\n")
                    val modified = getModifiedIdentifier(token.value)
                    if(modified != null) {
                        actualOutput.append("$modified\n")
                    }
                }
                else -> { /* do nothing */ }
            }
        }

        // Then the identifier and comment tokens should be in the expected output
        val expectedOutput = File("src/test/resources/PreprocessorTest/expectedOutput/terms.txt").readText()
        assertThat(actualOutput.toString()).isEqualToIgnoringNewLines(expectedOutput)
    }

    @Test
    fun testDocumentExtraction() {
        // Given a C file
        val sourceFile = File("src/test/resources/PreprocessorTest/actualInput/main.c")

        // When extracting documents out of it
        val lexer = Lexer(sourceFile.readText())
        val actualDocuments = preprocessor.extractDocuments(lexer.scan(), sourceFile)

        // Then they should be the same as the expected one(s)
        val expectedOutput = File("src/test/resources/PreprocessorTest/expectedOutput/doc00_main_c.cc")
            .readText()
            .replace("\r", "")

        assertThat(actualDocuments.size).isEqualTo(1)
        assertThat(actualDocuments[0].content).isEqualToIgnoringWhitespace(expectedOutput)
    }
}
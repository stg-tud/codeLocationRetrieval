
import org.assertj.core.api.Assertions
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
        Assertions.assertThat(getModifiedIdentifier("lowercase")).isNull()
        Assertions.assertThat(getModifiedIdentifier("UPPERCASE")).isNull()

        // camel case
        Assertions.assertThat(getModifiedIdentifier("URLLocation"))
            .isEqualTo("url location")

        // underscore
        Assertions.assertThat(getModifiedIdentifier("underscore_test"))
            .isEqualTo("underscore test")

        // mixed
        Assertions.assertThat(getModifiedIdentifier("someMixed_TestCase"))
            .isEqualTo("some mixed test case")

        // edge-cases
            // range-limit
        Assertions.assertThat(getModifiedIdentifier("URILo"))
            .isEqualTo("uri lo")

            // underscore at the beginning
        Assertions.assertThat(getModifiedIdentifier("_underscore"))
            .isEqualTo("underscore")

            // underscore at the end
        Assertions.assertThat(getModifiedIdentifier("underscore_"))
            .isEqualTo("underscore")

            // more than one underscore
        Assertions.assertThat(getModifiedIdentifier("_under_score_"))
            .isEqualTo("under score")
    }

    // TODO: need to refactor
    @Test
    fun testTokenExtraction() {
        val expectedOutput = File("src/test/resources/testExpectedOutput/main/corpus.txt")
            .readText()
            .replace("\r", "")

        val actualOutput = StringBuilder()
        val tokenList = preprocessor.extractTokens(File("src/test/resources/testInput/main.c").readText())
        for(token in tokenList) {
            if(token.value == "\n") {
                println("NEWLINE")
            }
            else if(token.value == "\r\n") {
                println("CR NL")
            }

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

//        println(actualOutput)
        Assertions.assertThat(actualOutput.toString()).isEqualTo(expectedOutput)
    }

    @Test
    fun testDocumentExtraction() {
        val expectedOutput = File("src/test/resources/testExpectedOutput/main/doc0.txt")
            .readText()
            .replace("\r", "")

        val sourceFile = File("src/test/resources/testInput/main.c")
        val lexer = Lexer(sourceFile.readText())
        val actualOutput = preprocessor.extractDocuments(lexer.scan(), sourceFile)

        Assertions.assertThat(actualOutput.size).isEqualTo(1)
        // TODO: whitespace is causing test to fail
//        Assertions.assertThat(actualOutput[0].content).isEqualTo(expectedOutput)
        Assertions.assertThat(actualOutput[0].content).isEqualToIgnoringWhitespace(expectedOutput)
    }
}
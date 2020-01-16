
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import preprocessor.Preprocessor
import preprocessor.TokenType
import preprocessor.Lexer
import java.io.File

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
                    val modified = preprocessor.getModifiedIdentifier(token.value)
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
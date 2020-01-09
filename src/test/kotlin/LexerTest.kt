import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import preprocessor.Lexer
import preprocessor.TokenType
import java.io.File

class LexerTest {
    private val lexer = Lexer(File("src/test/resources/testInput/qsort.c").readText())

    @Test
    fun testTokenListSize() {
        val tokens = lexer.scan()
        Assertions.assertThat(tokens.size).isEqualTo(158)   // includes EOF token!
    }

    @Test
    fun testTokenCount() {
        val tokens = lexer.scan()

        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.EQUAL }.size).isEqualTo(7)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.LEFT_PAREN }.size).isEqualTo(13)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.RIGHT_PAREN }.size).isEqualTo(13)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.LEFT_BRACE }.size).isEqualTo(4)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.RIGHT_BRACE }.size).isEqualTo(4)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.SEMICOLON }.size).isEqualTo(17)

        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.IDENTIFIER }.size).isEqualTo(68)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.COMMENT }.size).isEqualTo(5)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.PP_DIRECTIVE }.size).isEqualTo(2)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.PP_END }.size).isEqualTo(2)

        // keywords
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.ENUM }.size).isEqualTo(1)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.STRUCT }.size).isEqualTo(1)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.VOID }.size).isEqualTo(3)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.CHAR }.size).isEqualTo(4)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.INT }.size).isEqualTo(9)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.FOR }.size).isEqualTo(1)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.IF }.size).isEqualTo(2)
        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.RETURN }.size).isEqualTo(1)

        Assertions.assertThat(tokens.filter { it.tokenType == TokenType.EOF }.size).isEqualTo(1)
    }
}
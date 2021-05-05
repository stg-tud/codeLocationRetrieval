import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import preprocessor.FileKind
import preprocessor.Lexer
import preprocessor.TokenType
import java.io.File

class LexerTest {
    private val lexer = Lexer(File("src/test/resources/LexerTest/actualInput/qsort.c").readText(),"", FileKind.Source)

    @Test
    fun testTokenListSize() {
        val tokens = lexer.scan()
        assertThat(tokens.size).isEqualTo(105)   // includes EOF token!
    }

    @Test
    fun testTokenCount() {
        val tokens = lexer.scan()

        assertThat(tokens.filter { it.tokenType == TokenType.LEFT_BRACE }.size).isEqualTo(4)
        assertThat(tokens.filter { it.tokenType == TokenType.RIGHT_BRACE }.size).isEqualTo(4)

        assertThat(tokens.filter { it.tokenType == TokenType.IDENTIFIER }.size).isEqualTo(68)
        assertThat(tokens.filter { it.tokenType == TokenType.COMMENT }.size).isEqualTo(5)
        assertThat(tokens.filter { it.tokenType == TokenType.PP_DIRECTIVE }.size).isEqualTo(2)

        // keywords
        assertThat(tokens.filter { it.tokenType == TokenType.ENUM }.size).isEqualTo(1)
        assertThat(tokens.filter { it.tokenType == TokenType.STRUCT }.size).isEqualTo(1)
        assertThat(tokens.filter { it.tokenType == TokenType.VOID }.size).isEqualTo(3)
        assertThat(tokens.filter { it.tokenType == TokenType.CHAR }.size).isEqualTo(4)
        assertThat(tokens.filter { it.tokenType == TokenType.INT }.size).isEqualTo(9)
        assertThat(tokens.filter { it.tokenType == TokenType.FOR }.size).isEqualTo(1)
        assertThat(tokens.filter { it.tokenType == TokenType.IF }.size).isEqualTo(2)
        assertThat(tokens.filter { it.tokenType == TokenType.RETURN }.size).isEqualTo(1)
    }
}
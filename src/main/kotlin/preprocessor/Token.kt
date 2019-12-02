package lexer

data class Token(val tokenType: TokenType, val value: String)

enum class TokenType {
    IDENTIFIER,
    COMMENT
}
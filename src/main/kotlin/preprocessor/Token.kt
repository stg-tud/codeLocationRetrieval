package preprocessor

data class Token(val tokenType: TokenType, val value: String)

enum class TokenType {
    IDENTIFIER,
    COMMENT
}

// these can't be identifiers
val reservedWords = listOf(
    "auto",     "double",   "int",      "struct",
    "break",    "else",     "long",     "switch",
    "case",     "enum",     "register", "typedef",
    "char",     "extern",   "return",   "union",
    "const",    "float",    "short",    "unsigned",
    "continue", "for",      "signed",   "void",
    "default",  "goto",     "sizeof",   "volatile",
    "do",       "if",       "static",   "while"
)

fun isKeyword(token: String): Boolean {
    return reservedWords.contains(token)
}
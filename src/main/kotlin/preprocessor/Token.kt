package preprocessor

data class Token(val tokenType: TokenType, val value: String, val startIndex: Int = -1)

enum class TokenType {
    // single character
    EQUAL,
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACE,
    RIGHT_BRACE,
    SEMICOLON,

    IDENTIFIER,
    COMMENT,
    PP_DIRECTIVE,

    // keywords
    AUTO,       DOUBLE, INT,        STRUCT,
    BREAK,      ELSE,   LONG,       SWITCH,
    CASE,       ENUM,   REGISTER,   TYPEDEF,
    CHAR,       EXTERN, RETURN,     UNION,
    CONST,      FLOAT,  SHORT,      UNSIGNED,
    CONTINUE,   FOR,    SIGNED,     VOID,
    DEFAULT,    GOTO,   SIZEOF,     VOLATILE,
    DO,         IF,     STATIC,     WHILE,

    EOF
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
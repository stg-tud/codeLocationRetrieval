package preprocessor

data class Token(val tokenType: TokenType, val value: String, val startIndex: Int = -1)

enum class TokenType {
    // single character
    LEFT_BRACE,
    RIGHT_BRACE,

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

//    EOF
}
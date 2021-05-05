package preprocessor

data class Token(val value: String, val tokenType: TokenType, val startIndex: Int = -1, val location: Location)
enum class TokenMetaType {
    Kind,
    FileKind,
}

sealed class TermMetaContent
enum class Kind {
    Comment,
    Identifier
}

data class Location(val line: Int, val column: Int, val fileName: String, val meta: Map<TokenMetaType, Any> = mapOf()) {
    fun withMeta(type: TokenMetaType, content: Any): Location {
        return Location(line, column,fileName, meta + (type to content))
    }
}

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
    PP_END,         // marks the end of a directive

    // keywords
    AUTO, DOUBLE, INT, STRUCT,
    BREAK, ELSE, LONG, SWITCH,
    CASE, ENUM, REGISTER, TYPEDEF,
    CHAR, EXTERN, RETURN, UNION,
    CONST, FLOAT, SHORT, UNSIGNED,
    CONTINUE, FOR, SIGNED, VOID,
    DEFAULT, GOTO, SIZEOF, VOLATILE,
    DO, IF, STATIC, WHILE,

    EOF
}
package preprocessor

import preprocessor.TokenType.*

class Lexer(private val input: String) {
    // the list of tokens that is to be filled during the scan of the input
    private val tokens = ArrayList<Token>()

    // index pointers used throughout the scan
    private var startIndex = 0      // points at the beginning of the lexeme of the current token
    private var currentIndex = 0    // points at the character that is to be consumed next

    // map keywords to token types
    private val keywords: Map<String, TokenType?>

    init {
        keywords = mutableMapOf()
        keywords.apply {
            put("auto",        AUTO)
            put("break",       BREAK)
            put("case",        CASE)
            put("char",        CHAR)
            put("const",       CONST)
            put("continue",    CONTINUE)
            put("default",     DEFAULT)
            put("do",          DO)
            put("double",      DOUBLE)
            put("else",        ELSE)
            put("enum",        ENUM)
            put("extern",      EXTERN)
            put("float",       FLOAT)
            put("for",         FOR)
            put("goto",        GOTO)
            put("if",          IF)
            put("int",         INT)
            put("long",        LONG)
            put("register",    REGISTER)
            put("return",      RETURN)
            put("short",       SHORT)
            put("signed",      SIGNED)
            put("sizeof",      SIZEOF)
            put("static",      STATIC)
            put("struct",      STRUCT)
            put("switch",      SWITCH)
            put("typedef",     TYPEDEF)
            put("union",       UNION)
            put("unsigned",    UNSIGNED)
            put("void",        VOID)
            put("volatile",    VOLATILE)
            put("while",       WHILE)
        }
    }

    fun scan(): List<Token> {
        while(!isAtEnd()) {
            startIndex = currentIndex
            scanToken()
        }

        return tokens
    }

    // scans the next token and adds it to the [tokens]
    private fun scanToken() {
        val previousChar = advance()     // note: we have advanced the current pointer
        when(previousChar) {
            // simple stuff
            '{' -> tokens.add(Token(LEFT_BRACE, "{", startIndex))
            '}' -> tokens.add(Token(RIGHT_BRACE, "}", startIndex))

            // more complex cases
            '\'', '\"'  -> skipQuotes(previousChar)
            '#'         -> preprocessorDirective()
            '/' -> {
                /* check if comment */
                if(match('/')) {
                    lineComment()
                }
                else if(match('*')) {
                    blockComment()
                }
            }
            else -> {
                /* check if identifier */
                if(previousChar.isLetter() || previousChar == '_') {
                    // note that we only check the beginning char here, the rest is handled in Lexer#identifier()
                    identifier()
                }
            }
        }
    }

    // consumes an entire identifier (advances currentIndex accordingly) and adds it to the token list
    private fun identifier() {
        // advance pointer until one past the identifier
        while(!isAtEnd() && isAlphaNumeric(input[currentIndex])) {
            advance()
        }

        // check if lexeme is keyword or identifier
        val lexeme = input.substring(startIndex, currentIndex)
        var type = keywords[lexeme]
        if(type == null) {
            // the lexeme was not found in the [keywords] map, hence it must be an identifier
            type = IDENTIFIER
        }
        tokens.add(Token(type, lexeme, startIndex))
    }

    private fun lineComment() {
        // two slashes have already been matched and consumed
        val lexeme = StringBuilder("//")

        while(!isAtEnd() && !(match('\r') || match('\n'))) {
            lexeme.append(advance())
        }

        tokens.add(Token(COMMENT, lexeme.toString(), startIndex))
    }

    private fun blockComment() {
        // beginning of block comment has already been matched
        val lexeme = StringBuilder("/*")

        while(!(peek() == '*' && lookahead() == '/')) {
            lexeme.append(advance())
        }

        // consume end of comment: "*/"
        lexeme.append(advance())
        lexeme.append(advance())

        tokens.add(Token(COMMENT, lexeme.toString(), startIndex))
    }

    private fun preprocessorDirective() {
        // hash symbol has already been matched
        val lexeme = StringBuilder("#")

        // consume potential whitespace in-between: e.g. "#   include <stdio.h>"
        while(!isAtEnd() && peek().isWhitespace()) {
            // null directive: #\s*[\r\n|\n]
            if(peek() == '\r' || peek() == '\n') {
                tokens.add(Token(PP_DIRECTIVE, lexeme.toString(), startIndex))
                return
            }

            advance()
        }

        // the preprocessor directive (include, define, if, etc.)
        while(!isAtEnd() && !peek().isWhitespace()) {
            lexeme.append(advance())
        }

        // process #includes' here (easy way of getting past "..." or <...>)
        if(lexeme.toString() == "#include") {
            while(!isAtEnd() && peek() != '\n') {
                advance()
            }
        }

        tokens.add(Token(PP_DIRECTIVE, lexeme.toString(), startIndex))
    }

    private fun skipQuotes(singleOrDoubleQuote: Char) {
        while(!isAtEnd() && !match(singleOrDoubleQuote)) {
            if(peek() == '\\') {
                // advance twice if we see an escape character
                advance()
            }
            advance()
        }
    }

    // ==================================
    // == Fundamental helper functions ==
    // ==================================

    // return current char and advance the pointer to the next char to be consumed
    private fun advance() = input[currentIndex++]

    // if match, advance the [currentIndex]
    private fun match(expected: Char): Boolean {
        if(isAtEnd()) {
            return false
        }

        if(input[currentIndex] != expected) {
            return false
        }

        currentIndex++
        return true
    }

    private fun peek(): Char {
        return if(isAtEnd()) {
            0.toChar()
        }
        else {
            input[currentIndex]
        }
    }

    private fun lookahead(): Char {
        return if(currentIndex + 1 >= input.length) {
            0.toChar()
        }
        else {
            input[currentIndex + 1]
        }
    }

    private fun isAlphaNumeric(c: Char) = c.isLetterOrDigit() || c == '_'

    private fun isAtEnd() = currentIndex >= input.length
}
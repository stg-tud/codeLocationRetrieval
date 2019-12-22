package preprocessor


class Lexer(private val input: String) {
    // TODO: important, maybe give as command line option
    // e.g. in [gui.c] check line 280: /*AppData Rdata;  /* extern'd in mosaic.h */
    private val compilerAllowsNestedComments = false

    // the list of tokens that is to be filled during the scan of the input
    private val tokens = ArrayList<Token>()

    // index pointers used throughout the scan
    private var startIndex = 0      // points at the beginning of the lexeme of the current token
    private var currentIndex = 0    // points at the character that is to be consumed next

    // map keywords to token types
    private val keywords: Map<String, TokenType?>

    init {
        keywords = HashMap()
        keywords.put("auto",        TokenType.AUTO)
        keywords.put("break",       TokenType.BREAK)
        keywords.put("case",        TokenType.CASE)
        keywords.put("char",        TokenType.CHAR)
        keywords.put("const",       TokenType.CONST)
        keywords.put("continue",    TokenType.CONTINUE)
        keywords.put("default",     TokenType.DEFAULT)
        keywords.put("do",          TokenType.DO)
        keywords.put("double",      TokenType.DOUBLE)
        keywords.put("else",        TokenType.ELSE)
        keywords.put("enum",        TokenType.ENUM)
        keywords.put("extern",      TokenType.EXTERN)
        keywords.put("float",       TokenType.FLOAT)
        keywords.put("for",         TokenType.FOR)
        keywords.put("goto",        TokenType.GOTO)
        keywords.put("if",          TokenType.IF)
        keywords.put("int",         TokenType.INT)
        keywords.put("long",        TokenType.LONG)
        keywords.put("register",    TokenType.REGISTER)
        keywords.put("return",      TokenType.RETURN)
        keywords.put("short",       TokenType.SHORT)
        keywords.put("signed",      TokenType.SIGNED)
        keywords.put("sizeof",      TokenType.SIZEOF)
        keywords.put("static",      TokenType.STATIC)
        keywords.put("struct",      TokenType.STRUCT)
        keywords.put("switch",      TokenType.SWITCH)
        keywords.put("typedef",     TokenType.TYPEDEF)
        keywords.put("union",       TokenType.UNION)
        keywords.put("unsigned",    TokenType.UNSIGNED)
        keywords.put("void",        TokenType.VOID)
        keywords.put("volatile",    TokenType.VOLATILE)
        keywords.put("while",       TokenType.WHILE)
    }

    // TODO: keep track of functions and declaration blocks here or defer completely to parser?
    fun scan(): List<Token> {
        while(!isAtEnd()) {
            startIndex = currentIndex
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, ""))
        return tokens
    }

    private fun isAtEnd(): Boolean {
        return currentIndex >= input.length
    }

    // scans the next token and adds it to the [tokens]
    private fun scanToken() {
        val previousChar = advance()     // note: we have advanced the current pointer
        when(previousChar) {
            // simple stuff
            '=' -> tokens.add(Token(TokenType.EQUAL, "="))
            ',' -> tokens.add(Token(TokenType.COMMA, ","))
            '(' -> tokens.add(Token(TokenType.LEFT_PAREN, "("))
            ')' -> tokens.add(Token(TokenType.RIGHT_PAREN, "("))
            '{' -> tokens.add(Token(TokenType.LEFT_BRACE, "("))
            '}' -> tokens.add(Token(TokenType.RIGHT_BRACE, "("))

            // more complex cases
            '\'' -> {
                char()
            }
            '\"' -> {
                string()
            }
            '#' -> {
                preprocessorDirective()
            }
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
                if(previousChar.isDigit()) {
                    // skip numbers
                }
                else if(previousChar.isLetterOrDigit() || previousChar == '_') {
                    // note that we only check the beginning char here, the rest is handled in identifier()
                    identifier()
                }
            }
        }
    }

    // consumes an entire identifier (advances currentIndex accordingly) and adds it to the token list
    private fun identifier() {
        // advance pointer until past the identifier
        while(!isAtEnd() && isAlphaNumeric(input[currentIndex])) {
            advance()
        }

        // check if lexeme is keyword or identifier
        val lexeme = input.substring(startIndex, currentIndex)
        var type = keywords[lexeme]
        if(type == null) {
            type = TokenType.IDENTIFIER
        }
        tokens.add(Token(type, lexeme))
    }

    private fun lineComment() {
        // two slashes have already been matched and consumed
        val lexeme = StringBuilder("//")

        while(peek() != '\n' && peek() != 0.toChar()) {
            lexeme.append(advance())
        }

        tokens.add(Token(TokenType.COMMENT, lexeme.toString()))
    }

    private fun blockComment() {
        // beginning of block comment has already been matched
        val lexeme = StringBuilder("/*")

        // keep track of nested /**/ (init to 1 since we've seen the outermost /*)
        var nestCount = 1

        while(nestCount != 0) {
            val current = peek()
            val next = lookahead()

            if(compilerAllowsNestedComments && current == '/' && next == '*') {
                nestCount++
                lexeme.append("/*")
                currentIndex += 2   // skip next
            }
            else if(current == '*' && next == '/') {
                nestCount--
                lexeme.append("*/")
                currentIndex += 2   // skip next
            }
            else {
//                print(current)
                lexeme.append(current)
                currentIndex++
            }
        }

        tokens.add(Token(TokenType.COMMENT, lexeme.toString()))
    }

    private fun char() {
        // a char is either of the form 'a' or '\n'
        val lexeme = StringBuilder("'")

        // catch the '\' if it's there
        if(peek() == '\\') {
            lexeme.append(advance())
        }
        lexeme.append(advance())

        // TODO:
        tokens.add(Token(TokenType.PLACEHOLDER, lexeme.toString()))
    }

    // TODO: important, it works, but note that some (or all?) compilers allow multiline comments with \ at the end
    private fun string() {
        // consumed double quotes already
        val lexeme = StringBuilder("\"")

        while(peek() != '\"') {
            // escape character (next char will definitely not be the closing quote)
            if(peek() == '\\') {
                lexeme.append(advance()) // '\'
            }
            lexeme.append(advance())
        }
        // consume last quote
        lexeme.append(advance())

        // TODO: think about whether separate token types are needed for non ids/comments
        tokens.add(Token(TokenType.PLACEHOLDER, lexeme.toString()))
    }

    private fun preprocessorDirective() {
        val lexeme = StringBuilder("#")

        // the preprocessor directive (#include, #define, #if, etc.)
        while(!peek().isWhitespace()) {
            lexeme.append(advance())
        }

        // #includes' are always single line, so process it here (easy way of handling <...>)
        if(lexeme.toString() == "#include") {
            while(peek() != '\n') {
                lexeme.append(advance())
            }
        }

        // TODO: what token type
        tokens.add(Token(TokenType.PLACEHOLDER, lexeme.toString()))
    }

    // return current char and advance the pointer to the next char to be consumed
    private fun advance(): Char {
        currentIndex++
        return input[currentIndex - 1]
    }


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
            return input[currentIndex + 1]
        }
    }

    private fun isAlphaNumeric(c: Char): Boolean {
        return c.isLetterOrDigit() || c == '_'
    }
}
package preprocessor

import preprocessor.TokenType.*

class Parser(private val tokens: List<Token>,
             private val sourceCode: String,
             private val isHeaderFile: Boolean = false) {

    private val blocks = ArrayList<Block>()
    private var currentIndex = 0

    private var decCount = 0
    private var funcCount = 0

    fun parse(): List<Block> {
        // take header files as they are
        if(isHeaderFile) {
            val idsAndComments = tokens.filter { it.tokenType == IDENTIFIER || it.tokenType == COMMENT }
            blocks.add(Block(sourceCode, idsAndComments))
        }
        // for .c files find all function and declaration blocks
        else {
            while(!isAtEnd()) {
                parseToken()
            }
            println("declaration block count: $decCount")
            println("function definition count: $funcCount")
        }

        return blocks
    }

    private fun parseToken() {
        val previousToken = advance()

        // keep a global 'block' for identifiers and comments that do not belong to a function/declaration block?

        when(previousToken.tokenType) {
            EQUAL -> {
                // e.g. int myIntArray[] = { 1, 2, 3, 4, 5 }
                // skip until closing right brace hence avoid mistaking this for a declaration block
                if(match(LEFT_BRACE)) {
                    advanceToClosingBrace()
                }
            }
            ENUM, STRUCT, UNION -> {
                // e.g. enum { RED, GREEN } or enum color { RED, GREEN }
                if(match(LEFT_BRACE) || (match(IDENTIFIER) && match(LEFT_BRACE))) {
                    advanceToClosingBrace()
                }
            }
            RIGHT_PAREN -> {
                /* should be function ??? */
                var isFunction = false
                for(i in currentIndex..tokens.lastIndex) {
                    if(tokens[i].tokenType != COMMENT) {
                        isFunction = (tokens[i].tokenType == LEFT_BRACE)
                        break
                    }
                }

                if(isFunction) {
                    // determine the beginning of the function (including comment)
                    for(i in currentIndex downTo 0) {
                        val type = tokens[i].tokenType

                        if(type == RIGHT_BRACE || type == SEMICOLON || type == PP_END) {
                            break
                        }

                        currentIndex = i
                        if(type == IDENTIFIER || type == COMMENT) {
                            if(type == COMMENT && !tokens[i].value.startsWith("//")) {
                                break // if block comment, only include the closest one
                            }
                        }
                    }
                    val startIndex = tokens[currentIndex].startIndex
                    functionBlock(startIndex)
                    funcCount++
                }
            }
            LEFT_BRACE -> {
                /* should be declaration block ??? */
                declarationBlock(previousToken.startIndex)
                decCount++
            }
            else -> { /* do nothing */ }
        }
    }

    private fun declarationBlock(startIndex: Int) {
        // assumes we've already seen the opening brace '{'
        val idsAndComments = ArrayList<Token>()

        advanceToClosingBrace(idsAndComments)
        val token = previous()!!

        val endIndex = token.startIndex + token.value.length
        blocks.add(Block(sourceCode.substring(startIndex, endIndex), idsAndComments))
    }

    private fun functionBlock(startIndex: Int) {
        val idsAndComments = ArrayList<Token>()

        // consume return type, function name, parameter types and names, etc.
        var token: Token
        while(!isAtEnd() && !match(LEFT_BRACE)) {
            token = advance()
            when(token.tokenType) {
                IDENTIFIER, COMMENT -> {
                    idsAndComments.add(token)
                }
                else -> { /* do nothing */ }
            }
        }

        // now we're past the first '{' (which begins the function body)
        advanceToClosingBrace(idsAndComments)
        token = previous()!!

        val endIndex = token.startIndex + token.value.length
        blocks.add(Block(sourceCode.substring(startIndex, endIndex), idsAndComments))
    }

    /**
     * [idsAndComments] will hold the list of identifiers and comments for a block.
     * Use the default argument if they can be ignored (e.g. when skipping an array initialization).
     * Otherwise provide a list as an argument; the function will fill it.
     */
    private fun advanceToClosingBrace(idsAndComments: ArrayList<Token> = ArrayList<Token>()) {
        // assumes we've already seen the opening brace '{'
        var braceCount = 1

        var token: Token
        while(braceCount != 0) {
            token = advance()

            when(token.tokenType) {
                LEFT_BRACE -> braceCount++
                RIGHT_BRACE -> braceCount--
                IDENTIFIER, COMMENT -> idsAndComments.add(token)
                PP_DIRECTIVE -> {
                    if(token.value == "#if" || token.value == "#ifdef" || token.value == "#ifndef") {
                        token = advance()
                        braceCount += handleConditionalCompilation(token, idsAndComments)
                    }
                }
                else -> { /* do nothing */ }
            }
        }
    }

    /**
     * Returns the number of left braces ('{')
     * that have no matching right brace ('}')
     * within an #if|#ifdef|#ifndef case
     *
     * For example, for the following construct
     *
     *      #ifdef NAME
     *          if(someCondition) {
     *              doThis();
     *              if(someOtherCondition) {
     *      #else
     *          if(someCondition) {
     *              doThat();
     *              if(someOtherCondition) {
     *      #endif
     *                  otherStuff();
     *              }
     *              thisAndThat();
     *          }
     *
     * there are two '{' in the #ifdef case that aren't closed before arriving at #else,
     * hence the return value would be 2.
     *
     * Note: the #elif and #else cases will have the same number of unclosed open braces, if any.
     *       This is because for N unclosed '{' in #if..., there must be N '}' that closes them after the #endif.
     *       These N closing braces '}' must also match the #elif and #else cases.
     *       (-> This means we can skip brace counting in the #elif and #else cases)
     */
    private fun handleConditionalCompilation(startToken: Token, idsAndComments: ArrayList<Token>): Int {
        // #if/#ifdef/#ifndef already consumed
        var token = startToken
        var ppConditionalBraceCount = 0

        // advance until #elif, #else, or #endif
        while(!(token.tokenType == PP_DIRECTIVE
                    && (token.value == "#elif" || token.value == "#else" || token.value == "#endif"))) {
            when(token.tokenType) {
                LEFT_BRACE -> ppConditionalBraceCount++
                RIGHT_BRACE -> ppConditionalBraceCount--
                IDENTIFIER, COMMENT -> idsAndComments.add(token)
                PP_DIRECTIVE -> {
                    // possible recursion
                    if(token.value == "#if" || token.value == "#ifdef" || token.value == "#ifndef") {
                        token = advance()
                        ppConditionalBraceCount += handleConditionalCompilation(token, idsAndComments)
                    }
                }
                else -> { /* do nothing */ }
            }
            token = advance()
        }

        // token is either #elif, #else, or #endif
        if(token.value == "#elif" || token.value == "#else") {
            var nestCount = 0   // to keep track of the right #endif

            token = advance()
            while(!(token.tokenType == PP_DIRECTIVE && token.value == "#endif" && nestCount == 0)) {
                when(token.tokenType) {
                    IDENTIFIER, COMMENT -> idsAndComments.add(token)
                    PP_DIRECTIVE -> {
                        if(token.value == "#if" || token.value == "#ifdef" || token.value == "#ifndef") {
                            nestCount++
                        }
                        if(token.value == "#endif") {
                            nestCount--
                        }
                    }
                    else -> { /* do nothing */ }
                }
                token = advance()
            }
        }

        // #endif; done with conditional compiling
        advance()

        return ppConditionalBraceCount
    }

    private fun advance(): Token {
        currentIndex++
        return tokens[currentIndex - 1]
    }

    private fun peek(): Token {
        return tokens[currentIndex]
    }

    private fun previous(): Token? {
        if(currentIndex > 0) {
            return tokens[currentIndex - 1]
        }

        return null
    }

    private fun match(expected: TokenType): Boolean {
        if(isAtEnd() || tokens[currentIndex].tokenType != expected) {
            return false
        }

        currentIndex++
        return true
    }

    private fun isAtEnd(): Boolean {
        return currentIndex >= tokens.size
    }
}
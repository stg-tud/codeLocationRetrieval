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

    private fun isAtEnd(): Boolean {
        return currentIndex >= tokens.size
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
                if(peek().tokenType == LEFT_BRACE) {
                    // determine the beginning of the function (including comment)
                    for(i in currentIndex downTo 0) {
                        val type = tokens[i].tokenType

                        if(type == RIGHT_BRACE || type == SEMICOLON || type == PP_END) {
                            break
                        }

                        currentIndex = i
                        if(type == IDENTIFIER || type == COMMENT) {
                            // TODO: temporary "solution", only include the closest comment
                            if(type == COMMENT) {
                                break
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
        var braceCount = 1

        val idsAndComments = ArrayList<Token>()

        var token: Token
        do {
            token = advance()
            when(token.tokenType) {
                LEFT_BRACE -> braceCount++
                RIGHT_BRACE -> braceCount--
                IDENTIFIER, COMMENT -> idsAndComments.add(token)
                else -> { /* do nothing */ }
            }
        } while(braceCount != 0)

        // TODO: last token returned is '}' ??
        val endIndex = token.startIndex + token.value.length
        blocks.add(Block(sourceCode.substring(startIndex, endIndex), idsAndComments))
    }

    private fun functionBlock(startIndex: Int) {
        val idsAndComments = ArrayList<Token>()

        // consume return type, function name, parameter types and names, etc.
        var braceCount = 0
        var token = peek()
        while(!isAtEnd() && !match(LEFT_BRACE)) {
            token = advance()
            when(token.tokenType) {
                IDENTIFIER, COMMENT -> {
                    idsAndComments.add(token)
                }
                else -> { /* do nothing */ }
            }
        }
        braceCount++

        // here: one past '{', i.e. beginning of the body of the function
        while(braceCount != 0) {
            token = advance()

            when(token.tokenType) {
                LEFT_BRACE -> braceCount++
                RIGHT_BRACE -> braceCount--
                IDENTIFIER, COMMENT -> idsAndComments.add(token)
                PP_DIRECTIVE -> {
                    // handle conditional compiling, which can have un-balanced numbers of braces
                    if(token.value == "#if" || token.value == "#ifdef" || token.value == "#ifndef") {
                        var ppConditionalBraceCount = 0
                        token = advance()

                        // advance until #elif, #else, or #endif and check if brace count is > 0
                        while(!(token.tokenType == PP_DIRECTIVE
                                && (token.value == "#elif" || token.value == "#else" || token.value == "#endif"))) {
                            when(token.tokenType) {
                                LEFT_BRACE -> ppConditionalBraceCount++
                                RIGHT_BRACE -> ppConditionalBraceCount--
                                IDENTIFIER, COMMENT -> idsAndComments.add(token)
                                else -> { /* do nothing */ }
                            }
                            token = advance()
                        }

                        // here we are done with the if-condition of the pp-directive
                        braceCount += ppConditionalBraceCount

                        // token is currently either #elif or #else, so advance
                        token = advance()
                        while(!(token.tokenType == PP_DIRECTIVE && token.value == "#endif")) {
                            if(token.tokenType == IDENTIFIER || token.tokenType == COMMENT) {
                                idsAndComments.add(token)
                            }
                            token = advance()
                        }
                        // at #endif; done with conditional compiling
                        token = advance()
                    }
                }
                else -> { /* do nothing */ }
            }
        }

        val endIndex = token.startIndex + token.value.length
        blocks.add(Block(sourceCode.substring(startIndex, endIndex), idsAndComments))
    }

    private fun advance(): Token {
        currentIndex++
        return tokens[currentIndex - 1]
    }

    private fun advanceToClosingBrace() {
        // assumes we've already seen the opening brace '{'
        var braceCount = 1

        /*
            static XtActionsRec balloon_action[] = {
                {"BalloonHelpMe", (XtActionProc)BalloonHelpMe},
                {"UnBalloonHelpMe", (XtActionProc)UnBalloonHelpMe}
            };
         */

        var token: Token // advance here or not?
        do {
            token = advance()

            if(token.tokenType == LEFT_BRACE) {
                braceCount++
            }

            if(token.tokenType == RIGHT_BRACE) {
                braceCount--
            }
        } while(braceCount != 0)
    }

    private fun peek(): Token {
        return tokens[currentIndex]
    }

    private fun match(expected: TokenType): Boolean {
        if(isAtEnd() || tokens[currentIndex].tokenType != expected) {
            return false
        }

        currentIndex++
        return true
    }
}
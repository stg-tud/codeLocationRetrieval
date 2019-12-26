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
            RIGHT_PAREN -> {
                /* should be function ??? */
                if(peek().tokenType == LEFT_BRACE) {
                    // determine the beginning of the function
                    for(i in currentIndex downTo 0) {
                        val type = tokens[i].tokenType

                        if(type == RIGHT_BRACE || type == SEMICOLON) {
                            break
                        }

                        if(type == IDENTIFIER || type == COMMENT) {
                            currentIndex = i
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

        // has not consumed any brace when entering here
        var braceCount = 0
        var token = peek()
        while(!match(LEFT_BRACE)) {
            when(token.tokenType) {
                IDENTIFIER, COMMENT -> {
                    idsAndComments.add(token)
                }
                else -> { /* do nothing */ }
            }
            token = advance()
        }
        braceCount++

        // here: one past '{'
        while(braceCount != 0) {
            token = advance()

            when(token.tokenType) {
                LEFT_BRACE -> braceCount++
                RIGHT_BRACE -> braceCount--
                IDENTIFIER, COMMENT -> idsAndComments.add(token)
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
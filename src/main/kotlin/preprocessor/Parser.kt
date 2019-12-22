package preprocessor

import preprocessor.TokenType.*

class Parser(private val tokens: List<Token>, private val sourceCode: String) {

    private val blocks = ArrayList<Block>()
    private var currentIndex = 0
    private val returnTypes = listOf(IDENTIFIER, VOID, CHAR, SHORT,
        INT, LONG, FLOAT, DOUBLE, SIGNED, UNSIGNED)

    private var decCount = 0
    private var funcCount = 0

    fun parse(): List<Block> {
        while(!isAtEnd()) {
            parseToken()
        }
        println("declaration block count: $decCount")
        println("function definition count: $funcCount")
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
            LEFT_PAREN -> {}
            RIGHT_PAREN -> {
                /* should be function ??? */
                if(peek().tokenType == LEFT_BRACE) {
                    // TODO: this is getting out of hand, there's still stuff like const and volatile that's not included
                    // TODO: instead, backtrack until you see a '}' or ';' ... that should work too?
                    // figure out startIndex; backtrack
                    for(i in currentIndex downTo 2) {
                        // ret_type func_name (
                        if(tokens[i].tokenType == LEFT_PAREN
                            && tokens[i - 1].tokenType == IDENTIFIER) {
                            // point at function name
                            currentIndex = i - 1
                            break
                        }
                    }

                    // return type is explicit
                    if(returnTypes.contains(tokens[currentIndex - 1].tokenType)) {
                        currentIndex--
                    }

                    // variants (TODO? C99 also has _Complex for floating point types)
                    when(tokens[currentIndex - 1].tokenType) {
                        LONG, SIGNED, UNSIGNED, STRUCT, UNION, ENUM -> {
                            currentIndex--
                        }
                        else -> {
                            /* do nothing */
                        }
                    }

                    // static or extern or comment?
                    when(tokens[currentIndex - 1].tokenType) {
                        STATIC, EXTERN, COMMENT -> {
                            // point at static/extern/comment
                            currentIndex--
                        }
                        else -> { /* do nothing */ }
                    }

                    // function comment? (maybe multiple single line or block comments? or allow max 1?)
                    while(tokens[currentIndex - 1].tokenType == COMMENT) {
                        currentIndex--
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
            RIGHT_BRACE -> {}

            COMMENT -> {}
            IDENTIFIER -> {
                // else, check if peek() is a '(' -> potential function -> need to check for ) { as well
            }

            // check all the keywords of the form __(...) { ... } -> skip those to avoid mistaking them for functions
            // actually: there should be no way to reach here because these can only be inside functions etc
            IF, ELSE, FOR, DO, WHILE -> {}

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
        println("($startIndex, $endIndex)")
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
        println("func($startIndex, $endIndex)")
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
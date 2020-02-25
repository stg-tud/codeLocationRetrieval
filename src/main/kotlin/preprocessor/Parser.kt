package preprocessor

import preprocessor.TokenType.*
import java.io.File

class Parser(private val tokens: List<Token>, private val sourceFile: File) {
    private val blocks = ArrayList<Block>()
    private var currentIndex = 0
    private val sourceCode = sourceFile.readText()

    fun parse(): List<Block> {
        // take header files as they are
        if(sourceFile.extension == "h") {
            val idsAndComments = tokens.filter { it.tokenType == IDENTIFIER || it.tokenType == COMMENT }
            blocks.add(Block(sourceCode, idsAndComments, sourceFile))
        }
        // for .c files find all function and declaration blocks
        else {
            while(!isAtEnd()) {
                parseToken()
            }

            // here reached end of file, check for comments and ids after the last block
            includeIdsAndCommentsAfterLastBlock()
        }

        return blocks
    }

    private fun parseToken() {
        val previousToken = advance()

        when(previousToken.tokenType) {
            LEFT_BRACE -> {
                generalBlock()
            }
            else -> { /* do nothing */ }
        }
    }

    // could be a function, declaration block, enum, struct, etc. A '{' marks a new block
    private fun generalBlock() {
        val idsAndComments = ArrayList<Token>()

        // find the end of the last block (or beginning of file - past first comment - if this is the first one)
        val startIndex = determineStartIndex()

        // start from the previous '}', i.e., take everything in-between this block and the previous one
        // so, consume everything up until the first '{'
        advanceToOpeningBrace(idsAndComments)

        // now we're past the first '{'
        advanceToClosingBrace(idsAndComments)
        // now we're one past the closing '}'
        val token = previous()!!    // token.type == '}'

        val endIndex = token.startIndex + token.value.length
        blocks.add(Block(sourceCode.substring(startIndex, endIndex), idsAndComments, sourceFile))
    }

    private fun determineStartIndex(): Int {
        for(i in currentIndex downTo 0) {
            val type = tokens[i].tokenType

            if(type == RIGHT_BRACE || (i == 0 && type == COMMENT)) {
                break
            }

            currentIndex = i
        }

        return tokens[currentIndex].startIndex
    }

    private fun advanceToOpeningBrace(idsAndComments: ArrayList<Token> = ArrayList()) {
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
    }

    /**
     * [idsAndComments] will hold the list of identifiers and comments for a block.
     * Use the default argument if they can be ignored (e.g. when skipping an array initialization).
     * Otherwise provide a list as an argument; the function will fill it.
     */
    private fun advanceToClosingBrace(idsAndComments: ArrayList<Token> = ArrayList()) {
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

    private fun includeIdsAndCommentsAfterLastBlock() {
        val idsAndComments = ArrayList<Token>()

        for(i in (currentIndex - 1) downTo 0) {
            val type = tokens[i].tokenType

            if(type == COMMENT || type == IDENTIFIER) {
                idsAndComments.add(tokens[i])
            }

            if(type == RIGHT_BRACE) {
                if(idsAndComments.isNotEmpty()) {
                    println(sourceFile.name)
                    val startIndex = tokens[i + 1].startIndex

                    // update the last block to include everything that follows till the end of file
                    val lastBlock = blocks.last()
                    val updatedContent = "${lastBlock.content}\n${sourceCode.substring(startIndex)}"
                    val updatedIdsAndComments = ArrayList<Token>(lastBlock.idsAndComments)
                    updatedIdsAndComments.addAll(idsAndComments)

                    val updatedLastBlock = Block(updatedContent, updatedIdsAndComments, sourceFile)

                    blocks.remove(lastBlock)
                    blocks.add(updatedLastBlock)
                }

                break
            }
        }
    }

    // ==================================
    // == Fundamental helper functions ==
    // ==================================

    private fun advance() = tokens[currentIndex++]

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

    private fun isAtEnd() = currentIndex >= tokens.size
}
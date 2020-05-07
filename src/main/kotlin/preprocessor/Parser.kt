package preprocessor

import preprocessor.TokenType.*
import java.io.File

class Parser(private val tokens: List<Token>, private val sourceFile: File) {
    private val documents = ArrayList<Document>()
    private var currentIndex = 0
    private val sourceCode = sourceFile.readText()

    // TODO:
    private var isBraceCountEnabled = true
    private var conditionalsNestCount = 0

    // on parse exception include everything starting from here
    private var startIndexInCaseOfFailure = 0

    fun parse(): List<Document> {
        // take header files as they are
        if(sourceFile.extension == "h") {
            val idsAndComments = tokens.filter { it.tokenType == IDENTIFIER || it.tokenType == COMMENT }
            if(idsAndComments.isNotEmpty()) {
                documents.add(Document(sourceCode, idsAndComments, sourceFile))
            }
        }
        // for .c files partition it around outermost blocks {...}
        else {
            try {
                while(!isAtEnd()) {
                    parseToken()
                }

                // here reached end of file, check for comments and ids after the last block
                includeIdsAndCommentsAfterLastBlock()
            }
            catch(e: Exception) {
                // TODO: some kind of synchronization/error-recovery would be nice ...
                // (For now: include everything starting from where the error occurred)
                println("${e.javaClass.simpleName}: Parse error for ${sourceFile.path}")

                val idsAndComments = mutableListOf<Token>()
                val startPos = tokens[startIndexInCaseOfFailure].startIndex
                for(i in startIndexInCaseOfFailure until tokens.size) {
                    val type = tokens[i].tokenType

                    if(type == IDENTIFIER || type == COMMENT) {
                        idsAndComments.add(tokens[i])
                    }
                }

                documents.add(Document(sourceCode.substring(startPos), idsAndComments, sourceFile))
            }

        }

        return documents
    }

    private fun parseToken() {
        val previousToken = advance()

        when(previousToken.tokenType) {
            LEFT_BRACE -> {
                generalBlock()
                startIndexInCaseOfFailure = currentIndex
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

        if(idsAndComments.isNotEmpty()) {
            documents.add(Document(sourceCode.substring(startIndex, endIndex), idsAndComments, sourceFile))
        }
    }

    private fun determineStartIndex(): Int {
        // currentIndex - 1 to start at LEFT_BRACE
        for(i in (currentIndex - 1) downTo 0) {
            val type = tokens[i].tokenType

            if(type == RIGHT_BRACE) {
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
                PP_DIRECTIVE -> {
                    // explanation:
                    // 1. we know we're done the moment we see the first LEFT_BRACE
                    // 2. if we see this LEFT_BRACE within an #if[(n)def], then there will be others within an #elif/#else
                    //  -> in those cases their LEFT_BRACE should not be counted
                    when(token.value) {
                        "#if", "#ifdef", "#ifndef" -> {
                            conditionalsNestCount++
                            isBraceCountEnabled = false
                        }
                        "#endif" -> {
                            conditionalsNestCount--
                            isBraceCountEnabled = true
                        }
                    }
                }
                else -> { /* do nothing */ }
            }
        }
    }

    /**
     * [idsAndComments] will hold the list of identifiers and comments for a [document][Document].
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
                LEFT_BRACE -> {
                    if(isBraceCountEnabled) {
                        braceCount++
                    }
                }
                RIGHT_BRACE -> braceCount--
                IDENTIFIER, COMMENT -> idsAndComments.add(token)
                PP_DIRECTIVE -> {
                    if(token.value == "#if" || token.value == "#ifdef" || token.value == "#ifndef") {
                        token = advance()
                        braceCount += handleConditionalCompilation(token, idsAndComments)
                    }
                    else if(token.value == "#endif") {
                        conditionalsNestCount = Integer.max(0, conditionalsNestCount - 1)
                        if(conditionalsNestCount == 0) {
                            isBraceCountEnabled = true
                        }
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
//                    println("Contains Ids/Comments after last block: ${sourceFile.name}")
                    val startIndex = tokens[i + 1].startIndex

                    // update the last block to include everything that follows till the end of file
                    val lastBlock = documents.last()
                    val updatedContent = "${lastBlock.content}\n${sourceCode.substring(startIndex)}"
                    val updatedIdsAndComments = ArrayList<Token>(lastBlock.idsAndComments)
                    updatedIdsAndComments.addAll(idsAndComments)

                    val updatedLastBlock = Document(updatedContent, updatedIdsAndComments, sourceFile)

                    documents.remove(lastBlock)
                    documents.add(updatedLastBlock)
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
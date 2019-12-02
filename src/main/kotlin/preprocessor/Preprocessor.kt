package preprocessor

import lexer.Token
import lexer.TokenType
import java.io.File

object Preprocessor {
    private val idRegex = "[a-zA-Z_][a-zA-Z_0-9]*".toRegex()

    // these can't be identifiers
    private val reservedWords = listOf(
        "auto",     "double",   "int",      "struct",
        "break",    "else",     "long",     "switch",
        "case",     "enum",     "register", "typedef",
        "char",     "extern",   "return",   "union",
        "const",    "float",    "short",    "unsigned",
        "continue", "for",      "signed",   "void",
        "default",  "goto",     "sizeof",   "volatile",
        "do",       "if",       "static",   "while"
    )

    private fun isKeyword(token: String): Boolean {
        return reservedWords.contains(token)
    }

    private fun isWhitespace(c: Char): Boolean {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r'
    }

    enum class IdAndCommentState {
        DEFAULT,
        IDENTIFIER,                     // [a-zA-Z_][a-zA-Z_0-9]* AND not(Sring) AND not(Comment)
        POTENTIAL_COMMENT,              // / (i.e. one slash... next char decide which state to go)
        LINE_COMMENT,                   // // any single line comment
        BLOCK_COMMENT,                  // /* any block comment */
        POTENTIAL_END_BLOCK_COMMENT,    // * (i.e. single asterisk... next state decides whether comment ends or not)
        STRING,                         // "any text within double quotes" (or single quotes:) 's'
        ESCAPE_STRING,                  // \ (purpose: to not mistake \" or '\"' for beginning/end of string)
        CHAR,                           // analogous to STRING
        ESCAPE_CHAR,                    // analogous to ESCAPE_STRING
        PREPROCESSOR_DIRECTIVE,         // #...
        PREPROCESSOR_NEWLINE,           // \ at the end of a PP Directive marks a "newline", e.g. #define X \
    }

    fun tokenize(file: File): List<Token> {
        val tokens = ArrayList<Token>()

        val input = file.readText()
        var state = IdAndCommentState.DEFAULT
        val tokenBuffer = StringBuilder()

        var current: Char

        var i = 0
        while (i < input.length) {
            current = input[i]

            // determine the next state and store token, if necessary
            when (state) {
                IdAndCommentState.DEFAULT -> {
                    state = if (current == '\"') {
                        IdAndCommentState.STRING
                    } else if (current == '\'') {
                        IdAndCommentState.CHAR
                    } else if (current.isLetter() || current == '_') {
                        IdAndCommentState.IDENTIFIER
                    } else if (current == '/') {
                        IdAndCommentState.POTENTIAL_COMMENT
                    } else if (current == '#') {
                        IdAndCommentState.PREPROCESSOR_DIRECTIVE
                    } else {
                        IdAndCommentState.DEFAULT
                    }
                }
                IdAndCommentState.IDENTIFIER -> {
                    state = if (current.isLetterOrDigit() || current == '_') {
                        IdAndCommentState.IDENTIFIER
                    } else {
                        // make sure it's not a keyword
                        if (!isKeyword(tokenBuffer.toString())) {
                            tokens.add(Token(TokenType.IDENTIFIER, tokenBuffer.toString()))
                        }
                        tokenBuffer.setLength(0)
                        IdAndCommentState.DEFAULT
                    }
                }
                IdAndCommentState.POTENTIAL_COMMENT -> {
                    state = when (current) {
                        '/' -> IdAndCommentState.LINE_COMMENT
                        '*' -> IdAndCommentState.BLOCK_COMMENT
                        else -> {
                            tokenBuffer.setLength(0)    // clear out the / which we had written
                            IdAndCommentState.DEFAULT
                        }
                    }
                }
                IdAndCommentState.LINE_COMMENT -> {
                    state = when (current) {
                        '\r', '\n' -> {
                            tokens.add(Token(TokenType.COMMENT, tokenBuffer.toString()))
                            tokenBuffer.setLength(0)
                            IdAndCommentState.DEFAULT
                        }
                        else -> IdAndCommentState.LINE_COMMENT
                    }
                }
                IdAndCommentState.BLOCK_COMMENT -> {
                    state = when (current) {
                        '*' -> IdAndCommentState.POTENTIAL_END_BLOCK_COMMENT   // careful,
                        else -> IdAndCommentState.BLOCK_COMMENT
                    }
                }
                IdAndCommentState.POTENTIAL_END_BLOCK_COMMENT -> {
                    state = when (current) {
                        '/' -> {
                            tokenBuffer.append("/")
                            tokens.add(Token(TokenType.COMMENT, tokenBuffer.toString()))
                            tokenBuffer.setLength(0)
                            IdAndCommentState.DEFAULT
                        }
                        '*' -> IdAndCommentState.POTENTIAL_END_BLOCK_COMMENT
                        else -> IdAndCommentState.BLOCK_COMMENT
                    }
                }
                IdAndCommentState.STRING -> {
                    state = when (current) {
                        '\\' -> IdAndCommentState.ESCAPE_STRING
                        '\"' -> IdAndCommentState.DEFAULT
                        else -> IdAndCommentState.STRING
                    }
                }
                IdAndCommentState.ESCAPE_STRING -> {
                    state = IdAndCommentState.STRING
                }
                IdAndCommentState.CHAR -> {
                    state = when (current) {
                        '\\' -> IdAndCommentState.ESCAPE_CHAR
                        '\'' -> IdAndCommentState.DEFAULT
                        else -> IdAndCommentState.CHAR
                    }
                }
                IdAndCommentState.ESCAPE_CHAR -> {
                    state = IdAndCommentState.CHAR
                }
                IdAndCommentState.PREPROCESSOR_DIRECTIVE -> {
                    state = when (current) {
                        '\\' -> IdAndCommentState.PREPROCESSOR_NEWLINE
                        '\n' -> {
                            // the macro is always at 2nd position
                            // #define  CNAME {value, (expression)}
                            // #undef   macro_definition
                            // #ifdef   macro_definition
                            // #ifndef  macro_definition
                            with(tokenBuffer) {
                                if (startsWith("#define") || startsWith("#undef")
                                    || startsWith("#ifdef") || startsWith("#ifndef")
                                ) {
                                    val macro = this.split(" ")[1]
                                    tokens.add(Token(TokenType.IDENTIFIER, macro))
                                }
                                this.setLength(0)
                            }
                            IdAndCommentState.DEFAULT
                        }
                        else -> IdAndCommentState.PREPROCESSOR_DIRECTIVE
                    }
                }
                IdAndCommentState.PREPROCESSOR_NEWLINE -> {
                    state = IdAndCommentState.PREPROCESSOR_DIRECTIVE
                }
            }

            // handle input (TODO: if works -> make more concise)
            when (state) {
                IdAndCommentState.DEFAULT -> { /* do nothing */
                }
                IdAndCommentState.IDENTIFIER -> {
                    tokenBuffer.append(current)
                }
                IdAndCommentState.POTENTIAL_COMMENT -> {
                    tokenBuffer.append(current)
                }
                IdAndCommentState.LINE_COMMENT -> {
                    tokenBuffer.append(current)
                }
                IdAndCommentState.BLOCK_COMMENT -> {
                    tokenBuffer.append(current)
                }
                IdAndCommentState.POTENTIAL_END_BLOCK_COMMENT -> {
                    tokenBuffer.append(current)
                }
                IdAndCommentState.STRING -> { /* do nothing */
                }
                IdAndCommentState.ESCAPE_STRING -> { /* do nothing */
                }
                IdAndCommentState.CHAR -> { /* do nothing */
                }
                IdAndCommentState.ESCAPE_CHAR -> { /* do nothing */
                }
                IdAndCommentState.PREPROCESSOR_DIRECTIVE -> {
                    tokenBuffer.append(current)
                }
                IdAndCommentState.PREPROCESSOR_NEWLINE -> { /* do nothing */
                }
            }

            ++i
        }

        return tokens
    }

    // e.g. for "my_identifier" -> [my_identifier, my identifier]
    // e.g. for "URLLocation" -> [URLLocation, url location]
    fun getModifiedIdentifierList(identifier: String): List<String> {
        val modifiedIdentifierList = mutableListOf(identifier)

        // remove underscores: "my_identifier" -> "my identifier"
        if(identifier.contains("_")) {
            modifiedIdentifierList.add(identifier.toLowerCase().replace('_', ' '))
        }

        // handle camel case: "URLLocation" -> "url location"
        val camelCaseSb = StringBuilder()
        if(identifier.toUpperCase() != identifier && identifier.toLowerCase() != identifier) {
            val rangeLimit = identifier.indices.endInclusive - 1
            for(i in 0..rangeLimit) {
                val current = identifier[i]
                val next = identifier[i + 1]

                camelCaseSb.append(current.toLowerCase())
                if(current.isLowerCase() && next.isUpperCase()) {
                    camelCaseSb.append(" ")
                }
                // case for e.g. URILocation (I is upper, L is upper, but o is lower
                // so we've appended 'I' at this point, now put a space in-between I and L
                else if(i + 2 < rangeLimit && current.isUpperCase() && next.isUpperCase()
                    && identifier[i + 2].isLowerCase()) {
                    camelCaseSb.append(" ")
                }

                // check if we've reached the end, append last char as well
                if(i == rangeLimit) {
                    camelCaseSb.append(next.toLowerCase())
                }
            } // for()
            modifiedIdentifierList.add(camelCaseSb.toString())
        }

        return modifiedIdentifierList
    }

    enum class ExtractStates {
        DEFAULT,
        CLOSING_PARENTHESIS,
        POTENTIAL_FUNCTION,
        FUNCTION,
        DECLARATION_BLOCK,
        // differentiate between inner blocks to be able to determine which state to return to
        INNER_BLOCK_FUNCTION,                   // responsible for brace count
        INNER_BLOCK_DECLARATION,                // responsible for brace count
        // ignore strings, chars, and comments (that are not part of a block)
        STRING,
        STRING_ESCAPE,
        CHAR,
        CHAR_ESCAPE,
        POTENTIAL_COMMENT,
        LINE_COMMENT,
        BLOCK_COMMENT,
        POTENTIAL_END_BLOCK_COMMENT,
    }

    fun extractDocuments(input: String): List<String> {
        val documents = ArrayList<String>()

        var state = ExtractStates.DEFAULT
        val docSb = StringBuilder()

        // needed to determine the function header:
        val wordSb = StringBuilder()
        val wordList = ArrayList<String>() // keep of a list of seen tokens, each separated by blanks (space, tab,...)
        var potentialFunctionName = ""
        var potentialReturnType = ""
        var paramListStartIndex = 0

        // to determine when a function/declaration block ends
        var braceCount = 0

        var current: Char
        for(i in input.indices) {
            current = input[i]

            if(current == '(') {
                potentialFunctionName = wordSb.toString()
                potentialReturnType = wordList[wordList.size - 1]
                paramListStartIndex = i
            }

            if(!isWhitespace(current)) {
                wordSb.append(current)
            }
            // isWhitespace()...
            else if(wordSb.isNotBlank()){
                wordList.add(wordSb.toString())
                wordSb.setLength(0)
            }

            // handle state transition
            state = when(state) {
                ExtractStates.DEFAULT -> {
                    when(current) {
                        '{'     -> {
                            braceCount++
                            ExtractStates.DECLARATION_BLOCK
                        }
                        ')'     -> ExtractStates.CLOSING_PARENTHESIS
                        '\"'    -> ExtractStates.STRING
                        '\''    -> ExtractStates.CHAR
                        '/'     -> ExtractStates.POTENTIAL_COMMENT
                        else    -> ExtractStates.DEFAULT
                    }
                }
                ExtractStates.CLOSING_PARENTHESIS -> {
                    when(current) {
                        '{' -> {
                            if(!isKeyword(potentialFunctionName)) {
                                braceCount++
                                // return type and function name
                                docSb.append("$potentialReturnType $potentialFunctionName")
                                // parameter list (including parenthesis)
                                docSb.append(input.subSequence(paramListStartIndex, i))
                                ExtractStates.FUNCTION
                            }
                            else {
                                ExtractStates.DEFAULT
                            }
                        }
                        ')', '\r', '\n', ' ', '\t' -> ExtractStates.CLOSING_PARENTHESIS
                        else -> ExtractStates.DEFAULT
                    }
                }
                ExtractStates.POTENTIAL_FUNCTION -> {
                    when(current) {
                        '{' -> ExtractStates.FUNCTION // TODO: first check whether previous word is not a keyword
                        else -> ExtractStates.DEFAULT
                    }
                }
                ExtractStates.FUNCTION -> {
                    when(current) {
                        '{' -> ExtractStates.INNER_BLOCK_FUNCTION
                        '}' -> {
                            braceCount = 0
                            docSb.append("}")
                            documents.add(docSb.toString())
                            docSb.setLength(0)
                            ExtractStates.DEFAULT
                        }
                        else -> ExtractStates.FUNCTION
                    }
                }
                ExtractStates.DECLARATION_BLOCK -> {
                    when(current) {
                        '{' -> ExtractStates.INNER_BLOCK_DECLARATION
                        '}' -> {
                            braceCount = 0
                            docSb.append("}")
                            documents.add(docSb.toString())
                            docSb.setLength(0)
                            ExtractStates.DEFAULT
                        }
                        else -> ExtractStates.DECLARATION_BLOCK
                    }
                }
                ExtractStates.INNER_BLOCK_FUNCTION -> {
                    if(braceCount == 1) {
                        ExtractStates.FUNCTION
                    }
                    else {
                        ExtractStates.INNER_BLOCK_FUNCTION
                    }
                }
                ExtractStates.INNER_BLOCK_DECLARATION -> {
                    if(braceCount == 1) {
                        ExtractStates.DECLARATION_BLOCK
                    }
                    else {
                        ExtractStates.INNER_BLOCK_DECLARATION
                    }
                }
                ExtractStates.STRING -> {
                    when(current) {
                        '\\' -> ExtractStates.STRING_ESCAPE
                        '\"' -> ExtractStates.DEFAULT
                        else -> ExtractStates.STRING
                    }
                }
                ExtractStates.STRING_ESCAPE -> {
                    ExtractStates.STRING
                }
                ExtractStates.CHAR -> {
                    when(current) {
                        '\\' -> ExtractStates.CHAR_ESCAPE
                        '\"' -> ExtractStates.DEFAULT
                        else -> ExtractStates.CHAR
                    }
                }
                ExtractStates.CHAR_ESCAPE -> {
                    ExtractStates.CHAR
                }
                ExtractStates.POTENTIAL_COMMENT -> {
                    when(current) {
                        '/' -> ExtractStates.LINE_COMMENT
                        '*' -> ExtractStates.BLOCK_COMMENT
                        else -> ExtractStates.DEFAULT
                    }
                }
                ExtractStates.LINE_COMMENT -> {
                    when(current) {
                        '\r', '\n' -> ExtractStates.DEFAULT
                        else -> ExtractStates.LINE_COMMENT
                    }
                }
                ExtractStates.BLOCK_COMMENT -> {
                    when(current) {
                        '*' -> ExtractStates.POTENTIAL_END_BLOCK_COMMENT
                        else -> ExtractStates.BLOCK_COMMENT
                    }
                }
                ExtractStates.POTENTIAL_END_BLOCK_COMMENT -> {
                    when(current) {
                        '/' -> ExtractStates.DEFAULT
                        '*' -> ExtractStates.POTENTIAL_END_BLOCK_COMMENT
                        else -> ExtractStates.BLOCK_COMMENT
                    }
                }
            }

            // handle state action
            when(state) {
                ExtractStates.DEFAULT -> {}
                ExtractStates.CLOSING_PARENTHESIS -> {}
                ExtractStates.POTENTIAL_FUNCTION -> {}
                ExtractStates.FUNCTION -> {
                    docSb.append(current)
                }
                ExtractStates.DECLARATION_BLOCK -> {
                    docSb.append(current)
                }
                ExtractStates.INNER_BLOCK_FUNCTION -> {
                    docSb.append(current)
                    if(current == '{') {
                        braceCount++
                    }
                    else if(current == '}') {
                        braceCount--
                    }
                }
                ExtractStates.INNER_BLOCK_DECLARATION -> {
                    docSb.append(current)
                    if(current == '{') {
                        braceCount++
                    }
                    else if(current == '}') {
                        braceCount--
                    }
                }
                ExtractStates.STRING -> {}
                ExtractStates.STRING_ESCAPE -> {}
                ExtractStates.CHAR -> {}
                ExtractStates.CHAR_ESCAPE -> {}
                ExtractStates.POTENTIAL_COMMENT -> {}
                ExtractStates.LINE_COMMENT -> {}
                ExtractStates.BLOCK_COMMENT -> {}
                ExtractStates.POTENTIAL_END_BLOCK_COMMENT -> {}
            }
        }

        println("word list: $wordList")

        return documents
    }


}
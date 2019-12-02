package preprocessor

class IdAndCommentAutomata {

    enum class State {
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

    fun process(input: String): List<Token> {
        val tokens = ArrayList<Token>()

        var state = State.DEFAULT
        val tokenBuffer = StringBuilder()

        var current: Char

        for(i in input.indices) {
            current = input[i]

            // determine the next state and store token, if necessary
            state = when (state) {
                State.DEFAULT -> {
                    if (current == '\"') {
                        State.STRING
                    } else if (current == '\'') {
                        State.CHAR
                    } else if (current.isLetter() || current == '_') {
                        State.IDENTIFIER
                    } else if (current == '/') {
                        State.POTENTIAL_COMMENT
                    } else if (current == '#') {
                        State.PREPROCESSOR_DIRECTIVE
                    } else {
                        State.DEFAULT
                    }
                }
                State.IDENTIFIER -> {
                    if (current.isLetterOrDigit() || current == '_') {
                        State.IDENTIFIER
                    } else {
                        // make sure it's not a keyword
                        if (!isKeyword(tokenBuffer.toString())) {
                            tokens.add(Token(TokenType.IDENTIFIER, tokenBuffer.toString()))
                        }
                        tokenBuffer.setLength(0)
                        State.DEFAULT
                    }
                }
                State.POTENTIAL_COMMENT -> {
                    when (current) {
                        '/' -> State.LINE_COMMENT
                        '*' -> State.BLOCK_COMMENT
                        else -> {
                            tokenBuffer.setLength(0)    // clear out the / which we had written
                            State.DEFAULT
                        }
                    }
                }
                State.LINE_COMMENT -> {
                    when (current) {
                        '\r', '\n' -> {
                            tokens.add(Token(TokenType.COMMENT, tokenBuffer.toString()))
                            tokenBuffer.setLength(0)
                            State.DEFAULT
                        }
                        else -> State.LINE_COMMENT
                    }
                }
                State.BLOCK_COMMENT -> {
                    when (current) {
                        '*' -> State.POTENTIAL_END_BLOCK_COMMENT   // careful,
                        else -> State.BLOCK_COMMENT
                    }
                }
                State.POTENTIAL_END_BLOCK_COMMENT -> {
                    when (current) {
                        '/' -> {
                            tokenBuffer.append("/")
                            tokens.add(Token(TokenType.COMMENT, tokenBuffer.toString()))
                            tokenBuffer.setLength(0)
                            State.DEFAULT
                        }
                        '*' -> State.POTENTIAL_END_BLOCK_COMMENT
                        else -> State.BLOCK_COMMENT
                    }
                }
                State.STRING -> {
                    when (current) {
                        '\\' -> State.ESCAPE_STRING
                        '\"' -> State.DEFAULT
                        else -> State.STRING
                    }
                }
                State.ESCAPE_STRING -> {
                    State.STRING
                }
                State.CHAR -> {
                    when (current) {
                        '\\' -> State.ESCAPE_CHAR
                        '\'' -> State.DEFAULT
                        else -> State.CHAR
                    }
                }
                State.ESCAPE_CHAR -> {
                    State.CHAR
                }
                State.PREPROCESSOR_DIRECTIVE -> {
                    when (current) {
                        '\\' -> State.PREPROCESSOR_NEWLINE
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
                            State.DEFAULT
                        }
                        else -> State.PREPROCESSOR_DIRECTIVE
                    }
                }
                State.PREPROCESSOR_NEWLINE -> {
                    State.PREPROCESSOR_DIRECTIVE
                }
            }

            // handle input
            when (state) {
                State.IDENTIFIER,
                State.POTENTIAL_COMMENT,
                State.LINE_COMMENT,
                State.BLOCK_COMMENT,
                State.POTENTIAL_END_BLOCK_COMMENT,
                State.PREPROCESSOR_DIRECTIVE -> {
                    tokenBuffer.append(current)
                }
                else -> { /* do nothing */ }
            }
        }

        return tokens
    }
}
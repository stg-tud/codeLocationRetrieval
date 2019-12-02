package preprocessor

class DocumentAutomata {
    enum class State {
        DEFAULT,
        POTENTIAL_FUNCTION,             // seeing a closing parenthesis ')' might indicate end of function header
        FUNCTION_OR_DECLARATION_BLOCK,
        INNER_BLOCK,                    // responsible for brace count

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

    fun process(input: String): List<String> {
        val documents = ArrayList<String>()

        var state = State.DEFAULT
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
                potentialFunctionName = wordSb.toString()           // TODO will overwrite if paramlist has '(' (?)
                potentialReturnType = wordList[wordList.size - 1]   // TODO does "ret_type func_name()" always hold?
                paramListStartIndex = i
            }

            if(!current.isWhitespace()) {
                wordSb.append(current)
            }
            // isWhitespace()...
            else if(wordSb.isNotBlank()){
                wordList.add(wordSb.toString())
                wordSb.setLength(0)
            }

            // handle state transition
            state = when(state) {
                State.DEFAULT -> {
                    when(current) {
                        '{'     -> {
                            braceCount++
                            State.FUNCTION_OR_DECLARATION_BLOCK
                        }
                        ')'     -> State.POTENTIAL_FUNCTION
                        '\"'    -> State.STRING
                        '\''    -> State.CHAR
                        '/'     -> State.POTENTIAL_COMMENT
                        else    -> State.DEFAULT
                    }
                }
                State.POTENTIAL_FUNCTION -> {
                    when(current) {
                        '{' -> {
                            if(!isKeyword(potentialFunctionName)) {
                                braceCount++
                                // return type and function name
                                docSb.append("$potentialReturnType $potentialFunctionName")
                                // parameter list (including parenthesis)
                                docSb.append(input.subSequence(paramListStartIndex, i))
                                State.FUNCTION_OR_DECLARATION_BLOCK
                            }
                            else {
                                State.DEFAULT
                            }
                        }
                        ')', '\r', '\n', ' ', '\t' -> State.POTENTIAL_FUNCTION
                        else -> State.DEFAULT
                    }
                }
                State.FUNCTION_OR_DECLARATION_BLOCK -> {
                    when(current) {
                        '{' -> State.INNER_BLOCK
                        '}' -> {
                            braceCount = 0
                            docSb.append("}")
                            documents.add(docSb.toString())
                            docSb.setLength(0)
                            State.DEFAULT
                        }
                        else -> State.FUNCTION_OR_DECLARATION_BLOCK
                    }
                }
                State.INNER_BLOCK -> {
                    if(braceCount == 1) {
                        State.FUNCTION_OR_DECLARATION_BLOCK
                    }
                    else {
                        State.INNER_BLOCK
                    }
                }
                State.STRING -> {
                    when(current) {
                        '\\' -> State.STRING_ESCAPE
                        '\"' -> State.DEFAULT
                        else -> State.STRING
                    }
                }
                State.STRING_ESCAPE -> {
                    State.STRING
                }
                State.CHAR -> {
                    when(current) {
                        '\\' -> State.CHAR_ESCAPE
                        '\"' -> State.DEFAULT
                        else -> State.CHAR
                    }
                }
                State.CHAR_ESCAPE -> {
                    State.CHAR
                }
                State.POTENTIAL_COMMENT -> {
                    when(current) {
                        '/' -> State.LINE_COMMENT
                        '*' -> State.BLOCK_COMMENT
                        else -> State.DEFAULT
                    }
                }
                State.LINE_COMMENT -> {
                    when(current) {
                        '\r', '\n' -> State.DEFAULT
                        else -> State.LINE_COMMENT
                    }
                }
                State.BLOCK_COMMENT -> {
                    when(current) {
                        '*' -> State.POTENTIAL_END_BLOCK_COMMENT
                        else -> State.BLOCK_COMMENT
                    }
                }
                State.POTENTIAL_END_BLOCK_COMMENT -> {
                    when(current) {
                        '/' -> State.DEFAULT
                        '*' -> State.POTENTIAL_END_BLOCK_COMMENT
                        else -> State.BLOCK_COMMENT
                    }
                }
            }

            // handle state action
            when(state) {
                // write when we're in a function or declaration block
                State.FUNCTION_OR_DECLARATION_BLOCK -> {
                    docSb.append(current)
                }
                State.INNER_BLOCK -> {
                    docSb.append(current)
                    if(current == '{') {
                        braceCount++
                    }
                    else if(current == '}') {
                        braceCount--
                    }
                }
                else -> { /* do nothing */ }
            }
        }

        println("word list: $wordList")

        return documents
    }
}
package preprocessor

class Preprocessor {

    fun extractDocuments(input: String): List<String> {
        return DocumentAutomata().process(input)
    }

    fun extractDocs(tokens: List<Token>, sourceCode: String, isHeaderFile: Boolean): List<Block> {
        return Parser(tokens, sourceCode, isHeaderFile).parse()
    }

    fun extractTokens(input: String): List<Token> {
        return Lexer(input).scan()
//        return IdAndCommentAutomata().process(input)
    }

    fun getModifiedIdentifier(identifier: String): String? {
        val hasUnderscore = identifier.contains("_")
        val hasCamelCase = identifier.toUpperCase() != identifier && identifier.toLowerCase() != identifier

        // no underscore and no camel case -> nothing to do
        if(!hasUnderscore && !hasCamelCase) {
            return null
        }

        val resultSb = StringBuilder()

        // separate at appropriate positions
        if(hasCamelCase) {
            val rangeLimit = identifier.indices.endInclusive
            for(i in 0..(rangeLimit - 1)) {
                val current = identifier[i]
                val next = identifier[i + 1]

                resultSb.append(current)
                if(current.isLowerCase() && next.isUpperCase()) {
                    resultSb.append(" ")
                }
                // case for e.g. URILocation (I is upper, L is upper, but o is lower
                // so we've appended 'I' at this point, now put a space in-between I and L
                else if(i + 2 <= rangeLimit && current.isUpperCase() && next.isUpperCase()
                    && identifier[i + 2].isLowerCase()) {
                    resultSb.append(" ")
                }

                if(i == (rangeLimit - 1)) {
                    resultSb.append(next)
                }
            }
        }
        else {
            // no camel case -> just take the identifier as is
            resultSb.append(identifier)
        }

        var modifiedIdentifier = resultSb.toString().toLowerCase()
        if(hasUnderscore) {
            modifiedIdentifier = modifiedIdentifier.replace('_', ' ')
        }

        // also get rid of possible spaces at the beginning and/or end of the string
        return modifiedIdentifier.trim()
    }


    // e.g. for "my_identifier" -> [my_identifier, my identifier]
    // e.g. for "URLLocation" -> [URLLocation, url location]
    @Deprecated("Use Preprocessor#getModifiedIdentifier(String) instead ")
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
}
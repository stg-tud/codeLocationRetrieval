package preprocessor

class Preprocessor {

    fun extractDocuments(tokens: List<Token>, sourceCode: String, isHeaderFile: Boolean): List<Block> {
        return Parser(tokens, sourceCode, isHeaderFile).parse()
    }

    fun extractTokens(input: String): List<Token> {
        return Lexer(input).scan()
    }

    fun getModifiedIdentifier(identifier: String): String? {
        val hasUnderscore = identifier.contains("_")
        val hasCamelCase = identifier.toUpperCase() != identifier && identifier.toLowerCase() != identifier

        if(!hasUnderscore && !hasCamelCase) {
            // no underscore and no camel case -> nothing to do
            return null
        }

        val separateCamelCaseSb = StringBuilder()
        if(hasCamelCase) {
            // separate at appropriate positions
            val rangeLimit = identifier.indices.endInclusive
            for(i in 0..(rangeLimit - 1)) {
                val current = identifier[i]
                val next = identifier[i + 1]

                separateCamelCaseSb.append(current)
                if(current.isLowerCase() && next.isUpperCase()) {
                    separateCamelCaseSb.append(" ")
                }
                // case for e.g. URILocation (I is upper, L is upper, but o is lower
                // so we've appended 'I' at this point, now put a space in-between I and L
                else if(i + 2 <= rangeLimit && current.isUpperCase() && next.isUpperCase()
                    && identifier[i + 2].isLowerCase()) {
                    separateCamelCaseSb.append(" ")
                }

                if(i == (rangeLimit - 1)) {
                    separateCamelCaseSb.append(next)
                }
            }
        }
        else {
            // no camel case -> just take the identifier as is
            separateCamelCaseSb.append(identifier)
        }

        var modifiedIdentifier = separateCamelCaseSb.toString().toLowerCase()
        if(hasUnderscore) {
            modifiedIdentifier = modifiedIdentifier.replace('_', ' ')
        }

        // also get rid of possible spaces at the beginning and/or end of the string
        return modifiedIdentifier.trim()
    }
}
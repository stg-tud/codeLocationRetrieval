package preprocessor

class Preprocessor {

    fun tokenize(input: String): List<Token> {
        return IdAndCommentAutomata().process(input)
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

    fun extractDocuments(input: String): List<String> {
        return DocumentAutomata().process(input)
    }
}
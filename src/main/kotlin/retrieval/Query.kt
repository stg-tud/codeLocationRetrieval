package retrieval

import preprocessor.getModifiedIdentifier

class Query(input: String) {

    val queryTerms = input.split("\\s+".toRegex()).toMutableList()

    init {
        for(i in queryTerms.indices) {
            val modifiedTerm = getModifiedIdentifier(queryTerms[i])
            if(modifiedTerm != null) {
                queryTerms.addAll(modifiedTerm.split(" "))
            }

            queryTerms[i] = queryTerms[i].toLowerCase()
        }
    }
}
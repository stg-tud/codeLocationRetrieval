package retrieval

import preprocessor.getModifiedIdentifier

class Query(input: String) {

    val normalizedTerms = input.split("\\s+".toRegex()).toMutableList()

    init {
        for(i in normalizedTerms.indices) {
            val modifiedTerm = getModifiedIdentifier(normalizedTerms[i])
            if(modifiedTerm != null) {
                normalizedTerms.addAll(modifiedTerm.split(" "))
            }

            normalizedTerms[i] = normalizedTerms[i].toLowerCase()
        }
    }
}
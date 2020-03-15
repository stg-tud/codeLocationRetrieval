package retrieval

import org.apache.commons.math3.linear.MatrixUtils
import preprocessor.getModifiedIdentifier

class Query(input: String, val indexedTerms: Set<String> = emptySet()) {

    val normalizedTerms = input.split("\\s+".toRegex()).toMutableList()

    val queryVector = MatrixUtils.createRealVector(DoubleArray(indexedTerms.size) {0.0})

    init {
        normalizeTerms()

        for(queryTerm in normalizedTerms) {
            val termIdx = indexedTerms.indexOf(queryTerm)
            if(termIdx != -1) {
                // query term is contained in the TDM
                println("Setting query[$termIdx] (\"$queryTerm\") to ${queryVector.getEntry(termIdx) + 1}")
                queryVector.setEntry(termIdx, queryVector.getEntry(termIdx) + 1)
            }
            else {
                println("The term $queryTerm does not exist in the index")
            }
        }
    }

    fun isIndexed(queryTerm: String): Boolean {
        return indexedTerms.contains(queryTerm)
    }

    private fun normalizeTerms() {
        for(i in normalizedTerms.indices) {
            val modifiedTerm = getModifiedIdentifier(normalizedTerms[i])
            if(modifiedTerm != null) {
                normalizedTerms.addAll(modifiedTerm.split(" "))
            }

            normalizedTerms[i] = normalizedTerms[i].toLowerCase()
        }
    }
}
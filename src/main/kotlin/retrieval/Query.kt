package retrieval

import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealVector
import preprocessor.getModifiedIdentifier
import termdocmatrix.TermDocumentMatrix

class Query(input: String, private val tdm: TermDocumentMatrix) {

    val normalizedTerms = input.split("[\\s.!?,()%\"\'\\-]+".toRegex()).filter { it.isNotBlank() }.toMutableList()

    val queryVector: RealVector = MatrixUtils.createRealVector(DoubleArray(tdm.numOfTerms) {0.0})

    val indexedTerms = mutableSetOf<String>()

    init {
        normalizeTerms()

        // construct the query vector and store all indexed terms in a set (those that are in the term-document matrix)
        for(queryTerm in normalizedTerms) {
            val termIdx = tdm.terms.indexOf(queryTerm)
            if(termIdx != -1) {
                // query term is contained in the TDM
                queryVector.setEntry(termIdx, queryVector.getEntry(termIdx) + 1)

                // also store it in the indexedTerm set
                indexedTerms.add(queryTerm)
            }
        }
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
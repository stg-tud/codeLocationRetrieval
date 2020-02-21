package matrix

import preprocessor.Block

// N terms and M documents
class Matrix(private val terms: Set<String>, private val documents: List<Block>) {
    val numOfTerms = terms.size
    val numOfDocs = documents.size

    // a NxM array, initialized to all zeros
    val tdm = Array(numOfTerms) { DoubleArray(numOfDocs) {0.0}}

    init {
        populateTdm()
    }

    // TODO: implement some sort of strategy pattern to allow for different counting strategies
    // (currently: term frequencies, others: term occurrence and tf-idf)
    private fun populateTdm() {
        for(docIdx in 0..(documents.size - 1)) {
            val block = documents[docIdx]
            block.terms.forEach { term ->
                val termIdx = terms.indexOf(term)

                if(termIdx != -1) {
                    // term is contained in the corpus
                    tdm[termIdx][docIdx] += 1.0
                }
            }
        }
    }
}
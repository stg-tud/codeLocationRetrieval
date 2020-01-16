import preprocessor.Block

// N terms and M documents
class Matrix(private val terms: Set<String>, private val documents: List<Block>) {
    val numOfTerms = terms.size
    val numOfDocs = documents.size

    // a NxM array, initialized to all zeros
    val tdm = Array(numOfTerms) { Array(numOfDocs) {0}}

    init {
        populateTdm()
    }

    private fun populateTdm() {
        for(docIdx in 0..(documents.size - 1)) {
            val block = documents[docIdx]
            block.idsAndComments.forEach { token ->
                val termIdx = terms.indexOf(token.value)

                // term is contained in the corpus
                if(termIdx != -1) {
                    tdm[termIdx][docIdx] += 1
                }
            }
        }
    }
}
import preprocessor.Block

// N terms and M documents
class Matrix(val numOfTerms: Int, val numOfDocs: Int) {
    // a NxM array, initialized to all zeros
    val tdm = Array(numOfTerms) { Array(numOfDocs) {0}}

    fun populateTdm(corpus: Set<String>, documents: List<Block>) {
        for(docIdx in 0..(documents.size - 1)) {
            val block = documents[docIdx]
            block.idsAndComments.forEach { token ->
                val termIdx = corpus.indexOf(token.value)

                // term is contained in the corpus
                if(termIdx != -1) {
                    tdm[termIdx][docIdx] += 1
                }
            }
        }
    }
}
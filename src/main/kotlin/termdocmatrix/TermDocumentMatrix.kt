package termdocmatrix

import preprocessor.Document
import preprocessor.Term

// N terms and M documents
class TermDocumentMatrix {
    val terms: Array<Term>
    val documents: List<Document>

    val numOfTerms: Int
    val numOfDocs: Int

    // a NxM array, initialized to all zeros
    val data: Array<DoubleArray>

    // constructor that constructs the matrix based on the terms and documents
    constructor(terms: Set<Term>, documents: List<Document>) {
        this.terms = terms.toTypedArray()
        this.documents = documents

        numOfTerms = terms.size
        numOfDocs = documents.size

        data = Array(numOfTerms) { DoubleArray(numOfDocs) { 0.0 } }
        populateTdm()
    }

    // constructor which additionally takes in precomputed data
    // TODO: this is a bit dangerous, as the data array and the terms/documents collections are now decoupled
    // TODO: ... i.e., no guarantee that both actually correspond to each other (throw Exception?)
    // ...
    // -> possible solution to this: make 'data' a var with a private setter
    //      and provide a public weightEntries(TermWeightingStrategy) method in which we can alter 'data'
    //      (requires refactoring of TermWeightingStrategy though; postpone until we do weighting with query vectors)
    constructor(terms: Array<Term>, documents: List<Document>, data: Array<DoubleArray>) {
        if (data.size != terms.size || data[0].size != documents.size) {
            throw RuntimeException(
                "Dimensions of provided TDM do not match: " +
                        "Expected dimensions [${terms.size} x ${documents.size}, but was [${data.size} x ${data[0].size}"
            )
        }

        this.terms = terms.clone()
        this.documents = documents
        this.data = data

        numOfTerms = terms.size
        numOfDocs = documents.size
    }

    // populates the data using local term-frequency weights
    private fun populateTdm() {
        for (docIdx in documents.indices) {
            val block = documents[docIdx]
            block.terms.forEach { term ->
                val termIdx = terms.indexOfFirst { term.term == it.term }
                if (termIdx != -1) {
                    // term is contained in the corpus
                    data[termIdx][docIdx] += 1.0 * term.locations.size
                }
            }
        }
    }
}
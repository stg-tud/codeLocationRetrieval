package termdocmatrix.weighting

import termdocmatrix.TermDocumentMatrix

class TermFrequencyWeighting : TermWeightingStrategy {

    override fun weightEntries(matrix: TermDocumentMatrix): TermDocumentMatrix {
        // The matrix is constructed using tf weighting, so we can return it as is
        return matrix
    }
}
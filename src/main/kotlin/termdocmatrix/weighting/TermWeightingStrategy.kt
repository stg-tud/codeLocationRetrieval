package termdocmatrix.weighting

import termdocmatrix.TermDocumentMatrix

interface TermWeightingStrategy {
    fun weightEntries(matrix: TermDocumentMatrix): TermDocumentMatrix
}
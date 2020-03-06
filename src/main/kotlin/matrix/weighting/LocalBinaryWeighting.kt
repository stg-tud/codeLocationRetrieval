package matrix.weighting

import matrix.Matrix

// A binary weighting scheme:
// The entry (t,d) = 1, if term t occurs in document d at least once. Otherwise (t, d) = 0.
class LocalBinaryWeighting : TermWeightingStrategy {

    override fun weightEntries(matrix: Matrix): Matrix {
        val weightedData = Array(matrix.numOfTerms) { DoubleArray(matrix.numOfDocs) {0.0} }

        for(i in matrix.data.indices) {
            for(j in matrix.data[i].indices) {
                // TODO: Checking for equality is more efficient ... if(matrix.data[i][j] == 0.0) 0.0 else 1.0
                weightedData[i][j] = if(matrix.data[i][j] > 0.0) 1.0 else 0.0
            }
        }

        return Matrix(matrix.terms, matrix.documents, weightedData)
    }
}
package matrix.weighting

import matrix.Matrix

class TermFrequencyWeighting : TermWeightingStrategy {

    override fun weightEntries(matrix: Matrix): Matrix {
        // The matrix is constructed using tf weighting, so we can return it as is
        return matrix
    }
}
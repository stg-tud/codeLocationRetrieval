package matrix.weighting

import matrix.Matrix

interface TermWeightingStrategy {
    fun weightEntries(matrix: Matrix): Matrix
}
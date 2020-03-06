package matrix.weighting

import matrix.Matrix
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealVector

// Weights terms by the TF-IDF scheme
// It uses the inverse of the "document frequency" and not "collection frequency" as some papers do
// df(t): number of documents in which term t occurs
// cf(t): number of times term t occurs in the entire collection
class TfIdfWeighting : TermWeightingStrategy {

    override fun weightEntries(matrix: Matrix): Matrix {
        val weightedData = Array(matrix.numOfTerms) { DoubleArray(matrix.numOfDocs) {0.0} }


        for(i in matrix.data.indices) {
            val rowVectorForTermI = MatrixUtils.createRealVector(matrix.data[i])
            // df(t) = number of nonzero entries in that row
            val docFrequencyOfTermI = rowVectorForTermI.countNonZeroEntries().toDouble()

            for(j in matrix.data[i].indices) {
                val tf = matrix.data[i][j]
                val idf = Math.log10(matrix.numOfDocs / docFrequencyOfTermI)
                weightedData[i][j] = tf * idf
            }
        }

        return Matrix(matrix.terms, matrix.documents, weightedData)
    }
}

fun RealVector.countNonZeroEntries(): Int {
    return toArray().filter { it != 0.0 }.size
}
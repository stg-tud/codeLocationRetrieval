package termdocmatrix.weighting

import termdocmatrix.TermDocumentMatrix
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealVector

/*
 * Log Entropy = LOG * ENTROPY
 *
 * Log Entropy is defined as the product of the local weighting scheme l(t_d) and global weighting scheme g(t_d), where
 *
 *      l(t_d) = log(tf(t_d) + 1)
 *      g(t_d) = 1 + [ (1 / log(numOfDocs)) * sumOverDocs(p(t_d) * log(p(t_d)) ]
 *
 * with p(t_d) = tf(t_d) * cf(t_d) and cf(t_d) is the collection frequency of term t.
 *
 * Here, t_d denotes term t in document d
 *
 */
class LogEntropyWeighting : TermWeightingStrategy {

    override fun weightEntries(matrix: TermDocumentMatrix): TermDocumentMatrix {
        val weightedData = Array(matrix.numOfTerms) { DoubleArray(matrix.numOfDocs) {0.0} }

        for(i in matrix.data.indices) {
            val entropyForTermI = entropy(matrix.data[i], matrix.numOfDocs)

            for(j in matrix.data[i].indices) {
                val log = Math.log10(matrix.data[i][j] + 1)
                weightedData[i][j] = log * entropyForTermI
            }
        }

        return TermDocumentMatrix(matrix.terms, matrix.documents, weightedData)
    }

    private fun entropy(termRow: DoubleArray, numOfDocs: Int): Double {
        var entropy = 1.0

        // cf(t) = sum of all frequencies of a term t
        val collectionFrequency = termRow.sum()

        for(i in termRow.indices) {
            val tf = termRow[i]
            val p = tf / collectionFrequency

            if(p > 0) {
                entropy += (p * Math.log10(p)) / Math.log10(numOfDocs.toDouble())
            }
        }

        return entropy
    }
}
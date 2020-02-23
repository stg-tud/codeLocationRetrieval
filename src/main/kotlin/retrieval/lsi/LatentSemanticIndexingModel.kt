package retrieval.lsi

import main.display
import matrix.Matrix
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.SingularValueDecomposition
import retrieval.RetrievalResult
import java.util.ArrayList

class LatentSemanticIndexingModel(private val tdm: Matrix) {
    val svd = SingularValueDecomposition(MatrixUtils.createRealMatrix(tdm.data))

    fun retrieveDocuments(k: Int, query: List<String>, numberOfDocsToReturn: Int = 20): List<RetrievalResult> {
        // 1. compute SVD (check)

        // 2. truncate matrices
        val (Uk, Sk, VTk) = computeTruncatedMatrices(k)
        Sk.display("Sk")

        // 3. construct query vector
        val queryVector = MatrixUtils.createRealVector(DoubleArray(tdm.numOfTerms) {0.0})
        for(term in query) {
            val termIdx = tdm.terms.indexOf(term)
            if(termIdx != - 1) {
                // increment the term frequency
                println("Setting query[$termIdx] to ${queryVector.getEntry(termIdx) + 1}")
                queryVector.setEntry(termIdx, queryVector.getEntry(termIdx) + 1)
            }
            else {
                println("The term $term does not exist in the index")
            }
        }

        // 4. reduce query in LSI space as well
        val reducedQueryVector = Uk.multiply(MatrixUtils.inverse(Sk)).preMultiply(queryVector)

        // 5. compute similarity scores
        val listOfRetrievalResults = ArrayList<RetrievalResult>()
        for(i in 0..(VTk.columnDimension - 1)) {
            val docIVector = VTk.getColumnVector(i)
            val cosineScore = reducedQueryVector.unitVector().dotProduct(docIVector.unitVector())
            val retrievalResult = RetrievalResult(i, cosineScore, tdm.documents[i].sourceFile.name)
            listOfRetrievalResults.add(retrievalResult)
        }

        // 6. sort the results
        listOfRetrievalResults.sortByDescending { it.similarityScore }

        return listOfRetrievalResults.subList(0, numberOfDocsToReturn)
    }

    private fun computeTruncatedMatrices(k: Int): Triple<RealMatrix, RealMatrix, RealMatrix> {
        val truncU = Array(svd.u.rowDimension) { DoubleArray(k) {0.0} }
        svd.u.copySubMatrix(0, svd.u.rowDimension - 1, 0, k - 1, truncU)
        val Uk = MatrixUtils.createRealMatrix(truncU)

        val truncS = Array(k) { DoubleArray(k) {0.0} }
        svd.s.copySubMatrix(0, k - 1, 0, k - 1, truncS)
        val Sk = MatrixUtils.createRealMatrix(truncS)

        val truncVT = Array(k) { DoubleArray(svd.vt.columnDimension) {0.0} }
        svd.vt.copySubMatrix(0, k - 1, 0, svd.vt.columnDimension - 1, truncVT)
        val VTk = MatrixUtils.createRealMatrix(truncVT)

        return Triple(Uk, Sk, VTk)
    }
}
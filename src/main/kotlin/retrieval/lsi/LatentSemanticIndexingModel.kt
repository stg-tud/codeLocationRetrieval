package retrieval.lsi

import main.Options
import termdocmatrix.TermDocumentMatrix
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import retrieval.Query
import retrieval.RetrievalResult
import java.util.*

class LatentSemanticIndexingModel(private val tdm: TermDocumentMatrix) {
    // 1. compute SVD
    val svd = Svd(tdm, Options.outputSvdFile)

    fun retrieveDocuments(k: Int, query: Query): List<RetrievalResult> {
        // 2. truncate matrices
        val (uk, sk, vtk) = computeTruncatedMatrices(k)

        // 3. construct query vector
        val queryVector = MatrixUtils.createRealVector(DoubleArray(tdm.numOfTerms) {0.0})
        for(queryTerm in query.normalizedTerms) {
            val termIdx = tdm.terms.indexOf(queryTerm)
            if(termIdx != - 1) {
                // increment the term frequency
                println("Setting query[$termIdx] (\"$queryTerm\") to ${queryVector.getEntry(termIdx) + 1}")
                queryVector.setEntry(termIdx, queryVector.getEntry(termIdx) + 1)
            }
            else {
                println("The term $queryTerm does not exist in the index")
            }
        }

        // check if we even have a valid query
        if(queryVector.norm == 0.0) {
            return Collections.emptyList()
        }

        // 4. reduce query in LSI space as well
        val reducedQueryVector = uk.multiply(MatrixUtils.inverse(sk)).preMultiply(queryVector)

        // 5. compute similarity scores
        val listOfRetrievalResults = ArrayList<RetrievalResult>()
        for(i in 0..(vtk.columnDimension - 1)) {
            val docIVector = vtk.getColumnVector(i)
            val cosineScore = reducedQueryVector.unitVector().dotProduct(docIVector.unitVector())
            val retrievalResult = RetrievalResult(i, cosineScore, tdm.documents[i].sourceFile.name)
            listOfRetrievalResults.add(retrievalResult)
        }

        // 6. sort the results
        listOfRetrievalResults.sortByDescending { it.similarityScore }

        return listOfRetrievalResults
    }

    private fun computeTruncatedMatrices(k: Int): Triple<RealMatrix, RealMatrix, RealMatrix> {
        val truncU = Array(svd.u.rowDimension) { DoubleArray(k) {0.0} }
        svd.u.copySubMatrix(0, svd.u.rowDimension - 1, 0, k - 1, truncU)
        val uk = MatrixUtils.createRealMatrix(truncU)

        val truncS = Array(k) { DoubleArray(k) {0.0} }
        svd.s.copySubMatrix(0, k - 1, 0, k - 1, truncS)
        val sk = MatrixUtils.createRealMatrix(truncS)

        val truncVT = Array(k) { DoubleArray(svd.vt.columnDimension) {0.0} }
        svd.vt.copySubMatrix(0, k - 1, 0, svd.vt.columnDimension - 1, truncVT)
        val vtk = MatrixUtils.createRealMatrix(truncVT)

        return Triple(uk, sk, vtk)
    }
}
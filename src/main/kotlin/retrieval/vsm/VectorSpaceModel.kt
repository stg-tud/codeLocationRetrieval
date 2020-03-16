package retrieval.vsm

import org.apache.commons.math3.linear.MatrixUtils
import retrieval.Query
import retrieval.RetrievalResult
import termdocmatrix.TermDocumentMatrix
import java.util.*

class VectorSpaceModel(private val tdm: TermDocumentMatrix) {

    fun retrieveDocuments(query: Query): List<RetrievalResult> {
        val tdmAsRealMatrix = MatrixUtils.createRealMatrix(tdm.data)

        // get the query vector
        val queryVector = query.queryVector

        // check if we even have a valid query
        if(queryVector.norm == 0.0) {
            return Collections.emptyList()
        }

        // compute similarity scores
        val listOfRetrievalResults = ArrayList<RetrievalResult>()
        for(i in 0..(tdmAsRealMatrix.columnDimension - 1)) {
            val docIVector = tdmAsRealMatrix.getColumnVector(i)
            val cosineScore = queryVector.unitVector().dotProduct(docIVector.unitVector())
            val retrievalResult = RetrievalResult(i, cosineScore, tdm.documents[i].sourceFile.path)
            listOfRetrievalResults.add(retrievalResult)
        }

        // sort the results
        listOfRetrievalResults.sortByDescending { it.similarityScore }

        return listOfRetrievalResults
    }
}
package main.console

import main.Options
import preprocessor.Document
import retrieval.Location
import retrieval.Query
import retrieval.RetrievalResult
import termdocmatrix.TermDocumentMatrix

abstract class ConsoleApplication(protected val terms: Set<String>, protected val documents: List<Document>) {

    abstract fun start()

    /*
     * Provide some methods that may be useful for different kinds of consoles.
     * They are protected so that subclasses may change their behavior if necessary.
     */

    protected fun createTdm(): TermDocumentMatrix {
        val startTime = System.currentTimeMillis()

        // -1 because of empty line at the end (get rid of that)
        val matrix = TermDocumentMatrix(terms, documents)
        println("Number of terms = ${matrix.numOfTerms}")
        println("Number of documents = ${matrix.numOfDocs}")
        val tdm = Options.termWeightingStrategy.weightEntries(matrix)

        // time in seconds
        println("Time to create the TDM: ${(System.currentTimeMillis() - startTime) / 1000}s")

        return tdm
    }

    protected fun printResult(retrievalResult: RetrievalResult, tdm: TermDocumentMatrix, query: Query, rank: Int = 0) {
        val sb = StringBuilder("$rank.\t$retrievalResult\t\t")

        val documentLines = tdm.documents[retrievalResult.docIdx].content.lines()

        val locations = mutableListOf<Location>()
        for(queryTerm in query.indexedTerms) {
            var isDocumentContainsTerm = false

            sb.append("[$queryTerm:")
            for(i in documentLines.indices) {
                if(documentLines[i].contains(queryTerm, ignoreCase = true)) {
                    isDocumentContainsTerm = true
                    locations.add(Location(i+1, documentLines[i].indexOf(queryTerm, ignoreCase = true)))
                }
            }

            if(isDocumentContainsTerm) {
                // the term was found in this document
                // list.toString(): "[...]"
                // list.toString().substring(1): "...]"
                sb.append(" ${locations.toString().substring(1)},")
            }
            else {
                // the term was not found
                sb.append(" --],")
            }
        }

        sb.deleteCharAt(sb.lastIndexOf(","))

        println(sb)
    }
}
package main.console

import main.Options
import preprocessor.Document
import preprocessor.getTermsAndDocuments
import retrieval.Location
import retrieval.Query
import retrieval.RetrievalResult
import termdocmatrix.TermDocumentMatrix
import java.io.File
import java.lang.Exception

abstract class ConsoleApplication {

    protected val terms = HashSet<String>()
    protected val documents = ArrayList<Document>()

    init {
        processInput()
    }

    abstract fun start()

    private fun processInput() {
        val start = System.currentTimeMillis()

        val (terms, documents) = getTermsAndDocuments(inputRootDir = Options.inputRootDirectory, stopList = Options.stopList)

        if(documents.isEmpty()) {
            println("No C files were found. Please choose a directory that contains C files.")
            System.exit(0)
        }

        this.terms.addAll(terms)
        this.documents.addAll(documents)

        // write terms
        val termsFileWriter = Options.outputTermsFile.bufferedWriter()
        terms.forEach {
            termsFileWriter.write(it)
            termsFileWriter.newLine()
        }
        termsFileWriter.close()

        // write documents
        var docIndex = 0
        try {
            for(document in documents) {
                // in output/corpus: doc#_origFileName_origExtension.cc
                val docFile = File("${Options.outputCorpusDir}" +
                        "/doc${docIndex}_${document.sourceFile.nameWithoutExtension}_${document.sourceFile.extension}.cc")
                val docWriter = docFile.bufferedWriter()
                docWriter.write(document.content)
                docIndex++
                docWriter.close()
            }
        }
        catch(e: Exception) {
            throw e
        }

        val end = System.currentTimeMillis()
        println("Time to create the corpus: ${(end - start) / 1000f}s")
    }

    /*
     * ==================================================================================
     *
     * Provide some methods that may be useful for different kinds of consoles.
     * They are protected so that subclasses may change their behavior if necessary.
     *
     * ==================================================================================
     */

    protected fun createTdm(): TermDocumentMatrix {
        val startTime = System.currentTimeMillis()

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
package main.console

import main.Options
import retrieval.Query
import retrieval.lsi.LatentSemanticIndexingModel
import termdocmatrix.TermDocumentMatrix
import java.util.*

class LsiConsole : ConsoleApplication() {

    override fun start() {
        val tdm = createTdm()

        val startTime = System.currentTimeMillis()
        val lsiModel = LatentSemanticIndexingModel(tdm)
        Options.svdTimeInS = (System.currentTimeMillis() - startTime) / 1000
        println("Time(SVD): ${(System.currentTimeMillis() - startTime) / 1000}s")

        println("dim(U): ${lsiModel.svd.u.rowDimension} x ${lsiModel.svd.u.columnDimension}")
        println("dim(S): ${lsiModel.svd.s.rowDimension} x ${lsiModel.svd.s.columnDimension}")
        println("dim(VT): ${lsiModel.svd.vt.rowDimension} x ${lsiModel.svd.vt.columnDimension}")

        println("max singular value = ${lsiModel.svd.singularValues.first()}")
        println("min singular value = ${lsiModel.svd.singularValues.last()}")
        println("rank = ${lsiModel.svd.rank},\tnorm = ${lsiModel.svd.norm},\tcn = ${lsiModel.svd.conditionNumber}," +
                "\ticn = ${lsiModel.svd.inverseConditionNumber}")

        if(Options.isSvdOnly) {
            val out = String.format("[Corpus: %10d, TDM: %10d, SVD: %10d]",
                Options.corpusTimeInS, Options.tdmTimeInS, Options.svdTimeInS)
            Options.printWriterSvd.println(out)
            Options.printWriterSvd.close()
            System.exit(0)
        }

        // else query time
        queryLoop(lsiModel, tdm)
    }

    private fun queryLoop(lsiModel: LatentSemanticIndexingModel, tdm: TermDocumentMatrix) {
        val queryTerms = mutableListOf<String>()

        // different queries for different projects
        var queryString = ""
        val dirName = Options.outputRootDir.name
        when {
            dirName.contains("grbl") -> {
                queryString = "axis buffer arc helical bresenham motion linear command line position clock"
                queryTerms.addAll("axis buffer arc helical bresenham motion linear command line position clock".split(" "))
            }
            dirName.contains("ncsa") -> {
                queryString = "font size style small regular large family bold italics medium type"
                queryTerms.addAll("font size style small regular large family bold italics medium type".split(" "))
            }
            dirName.contains("svt") -> {
                queryString = "video encoder frame film picture channel display quantizer chroma mode driver"
                queryTerms.addAll("video encoder frame film picture channel display quantizer chroma mode driver".split(" "))
            }
            dirName.contains("obs") -> {
                queryString = "stream record lzma signal video graphics capture color converter audio encoder"
                queryTerms.addAll("stream record lzma signal video graphics capture color converter audio encoder".split(" "))
            }
        }

        println("Query terms: $queryTerms")

        // k values
        val rank = lsiModel.svd.rank
        val percentiles = listOf<Long>(
            1L,
            Math.round(rank * 0.1),
            Math.round(rank * 0.2),
            Math.round(rank * 0.3),
            Math.round(rank * 0.4),
            Math.round(rank * 0.5),
            Math.round(rank * 0.6),
            Math.round(rank * 0.7),
            Math.round(rank * 0.8),
            Math.round(rank * 0.9),
            rank.toLong())
        println(percentiles)

        var start: Long
        var end: Long
        val timeResults = mutableListOf<Long>()
        for(k in percentiles) {
            start = System.currentTimeMillis()
            val query = Query(queryString, tdm)
            val results = lsiModel.retrieveDocuments(k.toInt(), query)
            results.subList(0, 20).forEachIndexed { index, result ->
                println("${index + 1}.\t$result")
            }
            end = System.currentTimeMillis()
            timeResults.add((end - start) / 1000)
        }

        // write time results
        val out = String.format("SVD: %10d, " +
                "k=${percentiles[0]}: %10d, " +
                "k=${percentiles[1]}: %10d, " +
                "k=${percentiles[2]}: %10d, " +
                "k=${percentiles[3]}: %10d, " +
                "k=${percentiles[4]}: %10d, " +
                "k=${percentiles[5]}: %10d, " +
                "k=${percentiles[6]}: %10d, " +
                "k=${percentiles[7]}: %10d, " +
                "k=${percentiles[8]}: %10d, " +
                "k=${percentiles[9]}: %10d, " +
                "k=${percentiles[10]}: %10d, ",
                Options.svdTimeInS,
                timeResults[0],
                timeResults[1],
                timeResults[2],
                timeResults[3],
                timeResults[4],
                timeResults[5],
                timeResults[6],
                timeResults[7],
                timeResults[8],
                timeResults[9],
                timeResults[10])
        Options.printWriterQuery.println(out)
        Options.printWriterQuery.close()
    }
}
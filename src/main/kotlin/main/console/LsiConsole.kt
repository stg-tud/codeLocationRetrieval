package main.console

import main.Options
import retrieval.Location
import retrieval.Query
import retrieval.RetrievalResult
import retrieval.lsi.LsiModel
import termdocmatrix.TermDocumentMatrix
import java.io.File
import java.util.*

class LsiConsole : ConsoleApplication() {

    fun printTDM(tdm: TermDocumentMatrix) {
        for (i in 0..tdm.numOfTerms - 1) {
            val locations = tdm.terms[i].locations.joinToString { "(" + it.line + "," + it.column + ")" }
            println("%s & %s & %.2f \\\\ \\hline".format(tdm.terms[i].term, locations, tdm.data[i][0]))
        }
    }

    override fun start() {
        val tdm = createTdm()

        val startTime = System.currentTimeMillis()
        val lsiModel = LsiModel(tdm)
        println("Time(SVD): ${(System.currentTimeMillis() - startTime) / 1000}s")

        println("dim(U): ${lsiModel.svd.u.rowDimension} x ${lsiModel.svd.u.columnDimension}")
        println("dim(S): ${lsiModel.svd.s.rowDimension} x ${lsiModel.svd.s.columnDimension}")
        println("dim(VT): ${lsiModel.svd.vt.rowDimension} x ${lsiModel.svd.vt.columnDimension}")

        println("max singular value = ${lsiModel.svd.singularValues.first()}")
        println("min singular value = ${lsiModel.svd.singularValues.last()}")
        println(
            "rank = ${lsiModel.svd.rank},\tnorm = ${lsiModel.svd.norm},\tcn = ${lsiModel.svd.conditionNumber}," +
                    "\ticn = ${lsiModel.svd.inverseConditionNumber}"
        )

        val scanner = Scanner(System.`in`)
        val querySb = StringBuilder()
        var k: Int = -1

        // true on first iteration !
        var isNewK = true
        var isNewQuery = true
        var isFixedQuery = Options.searchTerms.isNotEmpty()
        if (isFixedQuery) {

            val query = Query(Options.searchTerms.joinToString(), tdm)
            val k90 = (lsiModel.svd.rank * 0.9).toInt()
            val results = lsiModel.retrieveDocuments(k90, query)
            saveAllLocations(query, tdm, results)

        } else {
            while (true) {
                if (isNewQuery) {
                    // read in user query
                    querySb.setLength(0)
                    print("Type in query: ")
                    while (scanner.hasNextLine()) {
                        val line = scanner.nextLine()
                        querySb.append(line)
                        if (querySb.isNotBlank()) {
                            break
                        }
                    }
                    println("User query is: $querySb")
                }

                if (isNewK) {
                    // print the singular values
//                val singularValues = lsiModel.svd.singularValues
//                for(i in singularValues.indices) {
//                    if(i > 0 && (i % 5 == 0)) {
//                        println()
//                    }
//
//                    print(String.format("%4d: %8.4f\t\t", i + 1, singularValues[i]))
//                }

                    // dimensionality reduction k in [1, rank]
                    do {
                        print("\nType in a value for k [1, ${lsiModel.svd.rank}]: ")
                        while (!scanner.hasNextInt()) {
                            print("Type in a value for k [1, ${lsiModel.svd.rank}]: ")
                            scanner.next()
                        }
                        k = scanner.nextInt()
                    } while (!(1 <= k && k <= lsiModel.svd.rank))
                }

                val query = Query(querySb.toString(), tdm)

                val results = lsiModel.retrieveDocuments(k, query)
                saveAllLocations(query, tdm, results)
                var startIdx = 0
                results.subList(startIdx, Integer.min(results.size, startIdx + 20))
                    .forEachIndexed { index, retrievalResult ->
                        printResult(
                            retrievalResult = retrievalResult,
                            tdm = tdm,
                            query = query,
                            rank = startIdx + index + 1
                        )
                    }

                println("Show more results? [y/n]")
                var next = scanner.next()
                while (next == "y" || next == "Y") {
                    startIdx += Integer.min(20, results.size - startIdx)
                    results.subList(startIdx, Integer.min(results.size, startIdx + 20))
                        .forEachIndexed { index, retrievalResult ->
                            printResult(
                                retrievalResult = retrievalResult,
                                tdm = tdm,
                                query = query,
                                rank = startIdx + index + 1
                            )
                        }

                    if (startIdx == results.size) {
                        println("All retrieved.")
                        break
                    }

                    println("Show more results? [y/n] $startIdx")
                    next = scanner.next()
                }


                println("\n\nType Q for a new query, or type K for the same query but another approximation: ")
                val input = scanner.next()
                when (input) {
                    "q", "Q" -> {
                        isNewQuery = true
                        isNewK = true
                    }
                    "k", "K" -> {
                        isNewQuery = false
                        isNewK = true
                    }
                    else -> return // finish main loop
                }
            }
        }
    }

    private fun saveAllLocations(query: Query, tdm: TermDocumentMatrix, results: List<RetrievalResult>) {
        val sb = StringBuilder()
        val threshold = 0.9
        sb.append("[")
        results.forEach { retrievalResult ->
            if (retrievalResult.similarityScore > threshold) {
                val documentLines = tdm.documents[retrievalResult.docIdx].content.lines()
                for (queryTerm in query.indexedTerms) {
                    var isDocumentContainsTerm = false
                    val locations = mutableListOf<Location>()
                    for (i in documentLines.indices) {
                        if (documentLines[i].contains(queryTerm, ignoreCase = true)) {
                            locations.add(Location(i + 1, documentLines[i].indexOf(queryTerm, ignoreCase = true)))
                        }
                    }
                    if (isDocumentContainsTerm) {
                        sb.append("{ \"term\" : \"$queryTerm\", \"file\" : \"${retrievalResult.sourceFileName}\", \"locations\" : [")
                        locations.forEach { loc ->
                            sb.append("{\"line\": ${loc.line}, \"column\" : ${loc.col}},")
                        }
                        sb.deleteCharAt(sb.length - 1)
                        sb.append("]},")
                    }
                }
            }
        }
        for (queryTerm in query.indexedTerms) {
            val term = this.terms.filter { it.term == queryTerm }.get(0)
            term.locations.groupBy { it.fileName }.forEach {
                sb.append("{ \"term\" : \"$queryTerm\", \"file\" : \"${it.key}\", \"locations\" : [")
                it.value.forEach { loc ->
                    sb.append("{\"line\": ${loc.line}, \"column\" : ${loc.column}, \"meta\" : {")
                    loc.meta.forEach { (metaKind, info) ->
                        sb.append("\"$metaKind\": \"$info\",")
                    }
                    if (loc.meta.isNotEmpty()) {
                        sb.deleteCharAt(sb.length - 1)
                    }
                    sb.append("}},")
                }
                sb.deleteCharAt(sb.length - 1)
                sb.append("]},")
            }
        }
        sb.deleteCharAt(sb.length - 1)
        sb.append("]")
        File(Options.outputLocationsJson).bufferedWriter().use { out ->
            out.write(sb.toString())
        }


    }
}
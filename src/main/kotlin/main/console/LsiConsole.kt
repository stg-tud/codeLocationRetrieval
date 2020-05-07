package main.console

import main.Options
import retrieval.Query
import retrieval.lsi.LatentSemanticIndexingModel
import java.util.*

class LsiConsole : ConsoleApplication() {

    override fun start() {
        val tdm = createTdm()

        val startTime = System.currentTimeMillis()
        val lsiModel = LatentSemanticIndexingModel(tdm)
        println("Time(SVD): ${(System.currentTimeMillis() - startTime) / 1000}s")
        Options.printOutputWriter.println("Time(SVD): ${(System.currentTimeMillis() - startTime) / 1000}s")

        println("dim(U): ${lsiModel.svd.u.rowDimension} x ${lsiModel.svd.u.columnDimension}")
        println("dim(S): ${lsiModel.svd.s.rowDimension} x ${lsiModel.svd.s.columnDimension}")
        println("dim(VT): ${lsiModel.svd.vt.rowDimension} x ${lsiModel.svd.vt.columnDimension}")
        Options.printOutputWriter.println("dim(U): ${lsiModel.svd.u.rowDimension} x ${lsiModel.svd.u.columnDimension}")
        Options.printOutputWriter.println("dim(S): ${lsiModel.svd.s.rowDimension} x ${lsiModel.svd.s.columnDimension}")
        Options.printOutputWriter.println("dim(VT): ${lsiModel.svd.vt.rowDimension} x ${lsiModel.svd.vt.columnDimension}")

        println("max singular value = ${lsiModel.svd.singularValues.first()}")
        println("min singular value = ${lsiModel.svd.singularValues.last()}")
        println("rank = ${lsiModel.svd.rank},\tnorm = ${lsiModel.svd.norm},\tcn = ${lsiModel.svd.conditionNumber}," +
                "\ticn = ${lsiModel.svd.inverseConditionNumber}")
        Options.printOutputWriter.println("rank = ${lsiModel.svd.rank}")
        Options.printOutputWriter.close()


        val scanner = Scanner(System.`in`)
        val querySb = StringBuilder()
        var k: Int = -1

        // true on first iteration !
        var isNewK = true
        var isNewQuery = true

        while(true) {
            if(isNewQuery) {
                // read in user query
                querySb.setLength(0)
                print("Type in query: ")
                while(scanner.hasNextLine()) {
                    val line = scanner.nextLine()
                    querySb.append(line)
                    if(querySb.isNotBlank()) {
                        break
                    }
                }
                println("User query: $querySb")
                println("Indexed query terms: ${Query(querySb.toString(), tdm).indexedTerms}")
            }

            if(isNewK) {
                // dimensionality reduction k in [1, rank]
                do {
                    print("\nType in a value for k [1, ${lsiModel.svd.rank}]: ")
                    while(!scanner.hasNextInt()) {
                        print("Type in a value for k [1, ${lsiModel.svd.rank}]: ")
                        scanner.next()
                    }
                    k = scanner.nextInt()
                } while(!(1 <= k && k <= lsiModel.svd.rank))
            }

            val query = Query(querySb.toString(), tdm)

            val results = lsiModel.retrieveDocuments(k, query)
            var startIdx = 0
            results.subList(startIdx, Integer.min(results.size, startIdx + 20)).forEachIndexed { index, retrievalResult ->
                printResult(retrievalResult = retrievalResult, tdm = tdm, query = query, rank = startIdx + index + 1)
            }

            println("Show more results? [y/n]")
            var next = scanner.next()
            yesorno@while(true) {
                when(next) {
                    "n", "N" -> break@yesorno
                    "y", "Y" -> {
                        startIdx += Integer.min(20, results.size - startIdx)
                        results.subList(startIdx, Integer.min(results.size, startIdx + 20)).forEachIndexed { index, retrievalResult ->
                            printResult(retrievalResult = retrievalResult, tdm = tdm, query = query, rank = startIdx + index + 1)
                        }

                        if(startIdx == results.size) {
                            println("All retrieved.")
                            break@yesorno
                        }

                        println("Show more results? [y/n]")
                        next = scanner.next()
                    }
                    else -> {
                        println("Show more results? [y/n]")
                        next = scanner.next()
                    }
                }
            }


            println("\n\nType Q for a new query, or type K for the same query but another approximation. " +
                    "Type 'exit' or 'stop' to stop the program: ")
            var input = scanner.next()
            qork@while(true) {
                when(input) {
                    "q", "Q" -> {
                        isNewQuery = true
                        isNewK = true
                        break@qork
                    }
                    "k", "K" -> {
                        isNewQuery = false
                        isNewK = true
                        break@qork
                    }
                    "exit", "stop" -> return // finish main loop
                    else -> {
                        println("\n\nType Q for a new query, or type K for the same query but another approximation. " +
                                "Type 'exit' or 'stop' to stop the program: ")
                        input = scanner.next()
                    }
                }
            }
        }
    }
}
package main.console

import retrieval.Query
import retrieval.vsm.VectorSpaceModel
import java.util.*

class VsmConsole : ConsoleApplication() {

    override fun start() {
        val tdm = createTdm()

        val startTime = System.currentTimeMillis()
        val vsmModel = VectorSpaceModel(tdm)
        println("Time(VSM): ${(System.currentTimeMillis() - startTime) / 1000}s")

        val scanner = Scanner(System.`in`)
        val querySb = StringBuilder()

        // true on first iteration !
        var isNewQuery = true

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

            val query = Query(querySb.toString(), tdm)

            val results = vsmModel.retrieveDocuments(query)
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

            println("\n\nType Q for a new query: ")
            val input = scanner.next()
            when (input) {
                "q", "Q" -> {
                    isNewQuery = true
                }
                else -> return // finish main loop
            }
        }
    }
}
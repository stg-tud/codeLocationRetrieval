package main

import java.io.File
import matrix.Matrix
import org.apache.commons.math3.linear.RealMatrix
import preprocessor.*
import retrieval.lsi.LatentSemanticIndexingModel
import java.lang.Exception
import java.util.*

val mBlocks = ArrayList<Block>()
val mTerms = HashSet<String>()
lateinit var mTdm: Matrix

fun main(args: Array<String>) {
//    val sourceCode = File("inputSandbox/gui.c").readText()
//    // lexer
//    val tokens = Lexer(sourceCode).scan()
////    tokens.forEach {
////        println(it)
////    }
//
//    // parser
//    val blocks = Parser(tokens, sourceCode).parse()
//    blocks.forEach {
//        println(it.content)
//    }

    bigInput()
    createTdm()
    mainLoop()
}

private fun bigInput() {
    val start = System.currentTimeMillis()

    val (terms, documents) = getTermsAndBlocks(rootDir = "inputBig/grbl")
    mTerms.addAll(terms)
    mBlocks.addAll(documents)

    // write corpus
    val termsFile = File("outputBig/terms.txt").bufferedWriter()
    mTerms.forEach {
        termsFile.write(it)
        termsFile.newLine()
    }
    termsFile.close()

    // write documents
    var docIndex = 0
    try {
        for(block in mBlocks) {
            val docFile = File("outputBig/docs/doc${docIndex}_${block.sourceFile.nameWithoutExtension}" +
                    "_${block.sourceFile.extension}.cc")
            docFile.parentFile.mkdirs()
            val docWriter = docFile.bufferedWriter()
            docWriter.write(block.content)
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

private fun createTdm() {
    val startTime = System.currentTimeMillis()

    // -1 because of empty line at the end (get rid of that)
    val matrix = Matrix(mTerms, mBlocks)
    println("Number of terms = ${matrix.numOfTerms}")
    println("Number of documents = ${matrix.numOfDocs}")
    mTdm = matrix

    // time in seconds
    println("Time to create the TDM: ${(System.currentTimeMillis() - startTime) / 1000}s")
}

private fun mainLoop() {
    val startTime = System.currentTimeMillis()
    val lsiModel = LatentSemanticIndexingModel(mTdm)
    println("Time(SVD): ${(System.currentTimeMillis() - startTime) / 1000}s")

    println("dim(U): ${lsiModel.svd.u.rowDimension} x ${lsiModel.svd.u.columnDimension}")
    println("dim(S): ${lsiModel.svd.s.rowDimension} x ${lsiModel.svd.s.columnDimension}")
    println("dim(VT): ${lsiModel.svd.vt.rowDimension} x ${lsiModel.svd.vt.columnDimension}")

    println("max singular value = ${lsiModel.svd.singularValues.first()}")
    println("min singular value = ${lsiModel.svd.singularValues.last()}")

    val scanner = Scanner(System.`in`)
    val querySb = StringBuilder()
    var k: Int = -1

    // true on first iteration !
    var isNewK = true
    var isNewQuery = true

    while(true) {
        if(isNewQuery) {
            // read in user query TODO: make it nicer, maybe BufferedReader.readLine() instead
            querySb.setLength(0)
            print("Type in query: ")
            while(scanner.hasNextLine()) {
                val line = scanner.nextLine()
                querySb.append(line)
                if(querySb.isNotBlank()) {
                    break
                }
            }
            println("User query is: $querySb")
        }

        if(isNewK) {
            // print the singular values
            val singularValues = lsiModel.svd.singularValues
            for(i in singularValues.indices) {
                if(i > 0 && (i % 4 == 0)) {
                    println()
                }

                print(String.format("%4d: %8.4f\t\t", i + 1, singularValues[i]))

            }

            // dimensionality reduction k in [1, S.rowDim]
            do {
                print("\nType in a value for k [1, ${lsiModel.svd.s.rowDimension}]: ")
                while(!scanner.hasNextInt()) {
                    print("Type in a value for k [1, ${lsiModel.svd.s.rowDimension }]: ")
                    scanner.next()
                }
                k = scanner.nextInt()
            } while(!(1 <= k && k <= lsiModel.svd.s.rowDimension))
        }

        // "normalize" query, e.g. make it all lowercase
        val queryWordList = querySb.split("\\s+".toRegex()).toMutableList()
        for(i in queryWordList.indices) {
            queryWordList[i] = queryWordList[i].toLowerCase()
        }
        println(queryWordList)

        val results = lsiModel.retrieveDocuments(k, queryWordList)
        results.subList(0, 20).forEach { println(it) }

        println("\n\nType Q for a new query, or type K for the same query but another approximation: ")
        val input = scanner.next()
        when(input) {
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

// extension function for printing Commons Math RealMatrix
fun RealMatrix.display(matrixName: String = "A") {
    val data = this.data

    println("$matrixName = ")
    for(i in data.indices) {
        for(j in data[i].indices) {
            print("%8.4f ".format(data[i][j]))
        }
        println()
    }

    println()
}
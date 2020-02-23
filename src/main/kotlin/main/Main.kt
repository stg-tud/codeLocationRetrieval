package main

import java.io.File
import matrix.Matrix
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.SingularValueDecomposition
import preprocessor.*
import retrieval.lsi.LatentSemanticIndexingModel
import java.lang.Exception
import java.util.*

val mBlocks = ArrayList<Block>()
val mCorpusSet = HashSet<String>()
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

    // current issues:
    // 2. comments and modified identifiers are treated as a single string
    //      2.1. e.g. should "average num" be treated as one term or as two?
    //      2.2 should each word in a comment be treated as a single term?
}

private fun bigInput() {
    val start = System.currentTimeMillis()

    val (terms, documents) = getTermsAndBlocks(rootDir = "inputBig/grbl")
    mCorpusSet.addAll(terms)
    mBlocks.addAll(documents)

    // write corpus
    val corpus = File("outputBig/corpus.txt").bufferedWriter()
    mCorpusSet.forEach {
        corpus.write(it)
        corpus.newLine()
    }
    corpus.close()

    // write documents
    var docIndex = 0
    try {
        for(block in mBlocks) {
            val docFile = File("outputBig/docs/doc${docIndex}_${block.sourceFile.nameWithoutExtension}" +
                    "_${block.sourceFile.extension}.txt")
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
    println("${(end - start) / 1000f}s")
}

private fun createTdm() {
    val startTime = System.currentTimeMillis()

    // -1 because of empty line at the end (get rid of that)
    val matrix = Matrix(mCorpusSet, mBlocks)
    println(matrix.numOfTerms)
    println(matrix.numOfDocs)
    mTdm = matrix

    // time in seconds
    println("${(System.currentTimeMillis() - startTime) / 1000}s")
//    println(Arrays.toString(matrix.data[5]))

    // print the matrix
//    for(i in 0..(matrix.numOfTerms - 1)) {
//        print(String.format("%-5.5s\t", corpus.lines()[i]))
//        for(j in 0..(matrix.numOfDocs - 1)) {
//            print("${matrix.data[i][j]} ")
//        }
//        println()
//    }
}

private fun mainLoop() {
    val startTime = System.currentTimeMillis()
    val lsiModel = LatentSemanticIndexingModel(mTdm)
    println("Time(SVD): ${(System.currentTimeMillis() - startTime) / 1000}s")

    val scanner = Scanner(System.`in`)
    val querySb = StringBuilder()
    var k: Int = -1

    // true on first iteration !
    var isNewK = true
    var isNewQuery = true

    while(true) {
        if(isNewK) {
            // dimensionality reduction k in [1, S.rowDim - 1]
            do {
                print("Type in a value for k (0, ${lsiModel.svd.s.rowDimension - 1}): ")
                while(!scanner.hasNextInt()) {
                    print("Type in a value for k (0, ${lsiModel.svd.s.rowDimension - 1}): ")
                    scanner.next()
                }
                k = scanner.nextInt()
            } while(!(0 <= k && k <= lsiModel.svd.s.rowDimension - 1))
        }

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

        val results = lsiModel.retrieveDocuments(k, querySb.split("\\s+".toRegex()))
        results.forEach { println(it) }

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
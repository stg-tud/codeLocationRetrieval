package main

import java.io.File
import matrix.Matrix
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.SingularValueDecomposition
import preprocessor.*
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

    // current issues:
    // 2. comments and modified identifiers are treated as a single string
    //      2.1. e.g. should "average num" be treated as one term or as two?
    //      2.2 should each word in a comment be treated as a single term?

    val cmMatrix = MatrixUtils.createRealMatrix(mTdm.tdm)

    val startTime = System.currentTimeMillis()
    val svd = SingularValueDecomposition(cmMatrix)
    println("Time(SVD): ${(System.currentTimeMillis() - startTime) / 1000}s")

    println("dim(S): ${svd.s.rowDimension} x ${svd.s.columnDimension}")
    svd.s.display("S")

//    svd.u.print()
//    println("dim(U): ${svd.u.rowDimension} x ${svd.u.columnDimension}")

//    svd.vt.print()
//    println("dim(V^T): ${svd.vt.rowDimension} x ${svd.vt.columnDimension}")

    // dimensionality reduction k in [1, S.rowDim - 1]
    var k: Int
    val scanner = Scanner(System.`in`)
    do {
        print("Type in a value for k (0, ${svd.s.rowDimension - 1}): ")
        while(!scanner.hasNextInt()) {
            print("Type in a value for k (0, ${svd.s.rowDimension - 1}): ")
            scanner.next()
        }
        k = scanner.nextInt()
    } while(!(0 <= k && k <= svd.s.rowDimension - 1))


    // calculate truncated matrices
    println("Calculating reduced matrices for k = $k")

    // Uk is a txk matrix
    val truncU = Array(svd.u.rowDimension) { DoubleArray(k) {0.0} }
    svd.u.copySubMatrix(0, svd.u.rowDimension - 1, 0, k - 1, truncU)
    val Uk = MatrixUtils.createRealMatrix(truncU)

    // Sk is a kxk matrix
    val truncS = Array(k) { DoubleArray(k) {0.0} }
    svd.s.copySubMatrix(0, k - 1, 0, k - 1, truncS)
    val Sk = MatrixUtils.createRealMatrix(truncS)

    // VTk is a kxd matrix
    val truncVT = Array(k) { DoubleArray(svd.vt.columnDimension) {0.0} }
    svd.vt.copySubMatrix(0, k - 1, 0, svd.vt.columnDimension - 1, truncVT)
    val VTk = MatrixUtils.createRealMatrix(truncVT)

    println("dim(Sk) = ${Sk.rowDimension} x ${Sk.columnDimension}")
    Sk.display("Sk")

    // read in user query TODO: make it nicer, maybe BufferedReader.readLine() instead
    val querySb = StringBuilder()
    print("Type in query: ")
    while(scanner.hasNextLine()) {
        val line = scanner.nextLine()
        querySb.append(line)
        if(querySb.isNotBlank()) {
            break
        }
    }
    println("User query is: $querySb")

    // construct query vector
    val queryVector = MatrixUtils.createRealVector(DoubleArray(mCorpusSet.size) {0.0} )
    for(term in querySb.split("\\s+".toRegex())) {
        val termIdx = mCorpusSet.indexOf(term)
        if(termIdx != -1) {
            println("Setting query[$termIdx] to ${queryVector.getEntry(termIdx) + 1}")
            queryVector.setEntry(termIdx, queryVector.getEntry(termIdx) + 1)
        }
    }

    // bring query vector to LSI space
    val reducedQueryVector = Uk.multiply(MatrixUtils.inverse(Sk)).preMultiply(queryVector)

    // compare with documents
    val pairListOfSimResults = ArrayList<Pair<Int, Double>>()
    for(i in 0..(VTk.columnDimension - 1)) {
        val docIVector = VTk.getColumnVector(i)
        val sim = reducedQueryVector.unitVector().dotProduct(docIVector.unitVector())
        pairListOfSimResults.add(Pair(i, sim))
    }

    // sort the results
    pairListOfSimResults.sortByDescending { it.second }

    // print the first N = 20 results
    for(i in 0..19) {
//        println("Document Index: ${pairListOfSimResults[i].first}, " +
//                "\tCosine Similarity: ${pairListOfSimResults[i].second}, " +
//                "\tFile Name: ${mBlocks[pairListOfSimResults[i].first].sourceFile.name}")
        val outputString = String.format(Locale.US, "Document Index: %5d, \tCosine Similarity: %8.4f, \tFile Name: %s",
            pairListOfSimResults[i].first,
            pairListOfSimResults[i].second,
            mBlocks[pairListOfSimResults[i].first].sourceFile.name)

        println(outputString)
    }
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
    println(Arrays.toString(matrix.tdm[5]))

    // print the matrix
//    for(i in 0..(matrix.numOfTerms - 1)) {
//        print(String.format("%-5.5s\t", corpus.lines()[i]))
//        for(j in 0..(matrix.numOfDocs - 1)) {
//            print("${matrix.tdm[i][j]} ")
//        }
//        println()
//    }
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
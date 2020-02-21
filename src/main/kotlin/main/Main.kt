package main

import java.io.File
import matrix.Matrix
import preprocessor.*
import java.lang.Exception
import java.util.*

val mBlocks = ArrayList<Block>()
val mCorpusSet = HashSet<String>()

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
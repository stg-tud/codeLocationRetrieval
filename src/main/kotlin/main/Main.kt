package main

import java.io.File
import Matrix
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

fun getCorpusAndBlocks(rootDir: String): Pair<Set<String>, List<Block>> {
    val corpusSet = mutableSetOf<String>()
    val blocks = mutableListOf<Block>()

    val preprocessor = Preprocessor()
    val dir = File(rootDir)
    dir.walkTopDown().forEach {
        // only operate on .h and .c files
        if(!(it.extension == "h" || it.extension == "c")) {
            return@forEach  // mimics a continue
        }

        val sourceCode = it.readText()

        // corpus
        val corpusList = ArrayList<String>()
        val tokens = preprocessor.extractTokens(sourceCode)
        for(token in tokens) {
            when(token.tokenType) {
                TokenType.COMMENT -> corpusList.add(token.value)
                TokenType.IDENTIFIER -> {
                    corpusList.add(token.value)
                    val modifiedId = preprocessor.getModifiedIdentifier(token.value)
                    if(modifiedId != null) {
                        corpusList.add(modifiedId)
                    }
                }
                else -> { /* do nothing */ }
            }
        }
        corpusSet.addAll(corpusList)

        // blocks
        blocks.addAll(preprocessor.extractDocuments(tokens, sourceFile = it))
    }

    return Pair(corpusSet, blocks)
}

private fun bigInput() {
    val start = System.currentTimeMillis()

    val (terms, documents) = getCorpusAndBlocks(rootDir = "inputBig/grbl")
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
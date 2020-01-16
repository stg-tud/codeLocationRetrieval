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
        val isHeaderFile = it.extension == "h"
        blocks.addAll(preprocessor.extractDocuments(tokens, sourceCode, isHeaderFile))
    }

    return Pair(corpusSet, blocks)
}

private fun bigInput() {
    val preprocessor = Preprocessor()

    val rootDir = "inputBig/grbl"
    val file = File(rootDir)

    val start = System.currentTimeMillis()

    var docIndex = 0
    val corpusList = ArrayList<String>()
    file.walkTopDown().forEach {
        // only operate on .h and .c files
        if(!(it.extension == "h" || it.extension == "c")) {
            return@forEach  // mimics a continue
        }

        val sourceCode = it.readText()

        // corpus
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

        // documents
        val isHeaderFile = it.extension == "h"
        try {
            val blocks = preprocessor.extractDocuments(tokens, sourceCode, isHeaderFile)
            mBlocks.addAll(blocks)
            for(block in blocks) {
                val docFile = File("outputBig/docs/doc${docIndex}_${it.nameWithoutExtension}" +
                        "_${it.extension}.txt")
                docFile.parentFile.mkdirs()
                val docWriter = docFile.bufferedWriter()
                docWriter.write(block.content)
                docIndex++
                docWriter.close()
            }
        }
        catch(e: Exception) {
            println(it.canonicalPath)
            throw e
        }
    }

    // write corpus
    val corpus = File("outputBig/corpus.txt").bufferedWriter()
    mCorpusSet.addAll(corpusList.toSet())
    corpusList.toSet().forEach {
        corpus.write(it)
        corpus.newLine()
    }
    corpus.close()

    val end = System.currentTimeMillis()
    println("${(end - start) / 1000f}s")
}

fun createTdm() {
    val corpus = File("outputBig/corpus.txt").readText()
    val docDir = File("outputBig/docs")

    // -1 because of empty line at the end (get rid of that)
    val matrix = Matrix(corpus.lines().size - 1, docDir.listFiles().size)
    println(matrix.numOfTerms)
    println(matrix.numOfDocs)

    val startTime = System.currentTimeMillis()

    // iterate over docs
    for(docIdx in 0..(mBlocks.size - 1)) {
        val block = mBlocks[docIdx]
        block.idsAndComments.forEach { token ->
            val termIdx = mCorpusSet.indexOf(token.value)

            // term is contained in the corpus
            if(termIdx != -1) {
                matrix.tdm[termIdx][docIdx] += 1
            }
        }
    }

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
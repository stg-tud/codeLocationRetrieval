package main

import java.io.File
import Matrix
import preprocessor.*
import java.util.*

fun main(args: Array<String>) {
    val sourceCode = File("inputSandbox/gui.c").readText()
    // lexer
    val tokens = Lexer(sourceCode).scan()
//    tokens.forEach {
//        println(it)
//    }

    // parser
    val blocks = Parser(tokens, sourceCode).parse()
    blocks.forEach {
        println(it.content)
    }

    // header files
//    val headerFile = File("inputSandbox/gui.h").readText()
//    val tkns = Lexer(headerFile).scan()
//    val idsAndComms = tkns.asSequence()
//        .filter { it.tokenType == TokenType.IDENTIFIER || it.tokenType == TokenType.COMMENT }
//        .toList()
//    val globalBlock = Block(headerFile, idsAndComms)
//    println(globalBlock)



//    sandboxInput()
//    sandboxInputNoFileWrite()
//    bigInput()
//    gradleBig()

    // current issues:
    // 1. creation of documents is faulty to begin with
    //      1.1. not extracting all functions
    //      1.2. return types may be missing
    //      1.3. function comments (a la JavaDoc) are not included
    //      1.4. things like array init are mistaken for declaration blocks
    // 2. comments and modified identifiers are treated as a single string
    //      2.1. e.g. should "average num" be treated as one term or as two?
    //      2.2 should each word in a comment be treated as a single term?
//    createTdm()
}

private fun sandboxInputNoFileWrite() {
    val preprocessor = Preprocessor()

    val filename = "gui"
    val file = File("inputSandbox/$filename.c")
    val fileContent = file.readText()

    val tokens = preprocessor.extractTokens(fileContent)
    tokens.forEach {
        println(it)
    }

    // write the documents (TODO: if file .h then automatically new doc)
    val documentList = preprocessor.extractDocuments(fileContent)
}

private fun sandboxInput() {
    val preprocessor = Preprocessor()

    val filename = "gui"
    val file = File("inputSandbox/$filename.c")
    val fileContent = file.readText()

    val tokens = preprocessor.extractTokens(fileContent)
    tokens.forEach {
        println(it)
    }

    // write the corpus
    val corpus = File("outputSandbox/corpus.txt").bufferedWriter()
    tokens.toSet().forEach {
        if(it.tokenType == TokenType.IDENTIFIER) {
            corpus.write(it.value)
            corpus.newLine()

            val modifiedId = preprocessor.getModifiedIdentifier(it.value)
            if(modifiedId != null) {
                corpus.write(modifiedId)
                corpus.newLine()
            }
        }
        else {
            corpus.write(it.value.replace("\n".toRegex(), "\\\\n"))
            corpus.newLine()
        }
    }
    corpus.close()

    // write the documents (TODO: if file .h then automatically new doc)
    val documentList = preprocessor.extractDocuments(fileContent)
    val filenameDoc = "outputSandbox/lexerDocs/$filename"
    for(i in documentList.indices) {
        // create the path
        val docFile = File("${filenameDoc}_doc_$i.txt")
        docFile.parentFile.mkdirs()

        // write to the file
        val bw = docFile.bufferedWriter()
        bw.write(documentList[i])
        bw.close()
    }
}

// recurse over the entire project
private fun bigInput() {
    val preprocessor = Preprocessor()

    val rootDir = "inputBig/ncsa-mosaic-master"
    val file = File(rootDir)

    val start = System.currentTimeMillis()

    var docIndex = 0
    val corpusList = ArrayList<String>()
    file.walkTopDown().forEach {
        // only operate on .h and .c files
        if(!(it.extension == "h" || it.extension == "c")) {
            return@forEach  // mimics a continue
        }

        val fileContent = it.readText()

        val tokens = preprocessor.extractTokens(fileContent)
        for(token in tokens) {
            if(token.tokenType == TokenType.IDENTIFIER) {
                val modifiedIds = preprocessor.getModifiedIdentifierList(token.value)
                for(id in modifiedIds) {
                    corpusList.add(id)
                }
            }
            else {
                corpusList.add(token.value)
            }
        }

        // .h files will be taken directly
        if(it.extension == "h") {
            val docFile = File("outputBig/docs/document${docIndex}_${it.nameWithoutExtension}.txt")
            docFile.parentFile.mkdirs()
            val docWriter = docFile.bufferedWriter()
            docWriter.write(fileContent)
            docIndex++
            docWriter.close()
        }
        // else (.c) process the file
        else {
            val documentList = preprocessor.extractDocuments(fileContent)
            for(doc in documentList) {
                val docFile = File("outputBig/docs/document${docIndex}_${it.nameWithoutExtension}.txt")
                docFile.parentFile.mkdirs()
                val docWriter = docFile.bufferedWriter()
                docWriter.write(doc)
                docIndex++
                docWriter.close()
            }
        }
    }

    val corpus = File("outputBig/corpus.txt").bufferedWriter()
    corpusList.toSet().forEach {
        corpus.write(it)
        corpus.newLine()
    }
    corpus.close()

    val end = System.currentTimeMillis()
    println("Time taken: ${(end - start) / 1000}s")
}

private fun gradleBig() {
    val preprocessor = Preprocessor()

    val rootDir = "inputBig/ncsa-mosaic-master"
    val file = File(rootDir)

    val start = System.currentTimeMillis()

    var docIndex = 0
    val corpusList = ArrayList<String>()
    file.walkTopDown().forEach {
        // only operate on .h and .c files
        if(!(it.extension == "h" || it.extension == "c")) {
            return@forEach  // mimics a continue
        }

        val fileContent = it.readText()

        val tokens = preprocessor.extractTokens(fileContent)
        for(token in tokens) {
            if(token.tokenType == TokenType.IDENTIFIER) {
                corpusList.add(token.value)
                val modifiedId = preprocessor.getModifiedIdentifier(token.value)
                if(modifiedId != null) {
                    corpusList.add(modifiedId)
                }
            }
            else {
                corpusList.add(token.value)
            }
        }

        // .h files will be taken directly
        if(it.extension == "h") {
            val docFile = File("outputBig/docs/document${docIndex}_${it.nameWithoutExtension}.txt")
            docFile.parentFile.mkdirs()
            val docWriter = docFile.bufferedWriter()
            docWriter.write(fileContent)
            docIndex++
            docWriter.close()
        }
        // else (.c) process the file
        else {
            val documentList = preprocessor.extractDocuments(fileContent)
            for(doc in documentList) {
                val docFile = File("outputBig/docs/document${docIndex}_${it.nameWithoutExtension}.txt")
                docFile.parentFile.mkdirs()
                val docWriter = docFile.bufferedWriter()
                docWriter.write(doc)
                docIndex++
                docWriter.close()
            }
        }
    }

    val corpus = File("outputBig/corpus.txt").bufferedWriter()
    corpusList.toSet().forEach {
        corpus.write(it)
        corpus.newLine()
    }
    corpus.close()

    val end = System.currentTimeMillis()
    println("Time taken: ${(end - start) / 1000}s")
}

private fun createTdm() {
    val corpus = File("outputSandbox/corpus.txt").readText()
    val docDir = File("outputSandbox/lexerDocs")

    // -1 because of empty line at the end (get rid of that)
    val matrix = Matrix(corpus.lines().size - 1, docDir.listFiles().size)
    println(matrix.numOfTerms)
    println(matrix.numOfDocs)

    val startTime = System.currentTimeMillis()

    // iterate over documents
    docDir.walk().forEach {
        val docIndex = it.nameWithoutExtension.substringAfterLast("_")

        if(it.isFile) {
            val fileContent = it.readText()

            // fill in the term-doc-matrix
            for((termIndex, term) in corpus.lines().withIndex()) {
                if(termIndex == corpus.lines().size - 1) {
                    break
                }

                if(fileContent.contains(term, ignoreCase = true)) {
                    matrix.tdm[termIndex][docIndex.toInt()] += 1
                }
            }
        }
    }
    // time in seconds
    println("${(System.currentTimeMillis() - startTime) / 1000}s")

    // print the matrix
    for(i in 0..(matrix.numOfTerms - 1)) {
        print(String.format("%-5.5s\t", corpus.lines()[i]))
        for(j in 0..(matrix.numOfDocs - 1)) {
            print("${matrix.tdm[i][j]} ")
        }
        println()
    }
}
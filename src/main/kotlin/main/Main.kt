package main

import preprocessor.TokenType
import preprocessor.Preprocessor
import java.io.File

fun main(args: Array<String>) {
    sandboxInput()

//    bigInput()
}


private fun sandboxInput() {
    val preprocessor = Preprocessor()

    val filename = "gui"
    val file = File("inputSandbox/$filename.c")
    val fileContent = file.readText()

    val tokens = preprocessor.tokenize(fileContent)
    tokens.forEach {
        println(it)
    }

    // write the corpus
    val corpus = File("outputSandbox/corpus.txt").bufferedWriter()
    tokens.toSet().forEach {
        if(it.tokenType == TokenType.IDENTIFIER) {
            for(id in preprocessor.getModifiedIdentifierList(it.value)) {
                corpus.write(id)
                corpus.newLine()
            }
        }
        else {
            corpus.write(it.value)
            corpus.newLine()
        }
    }
    corpus.close()

    // write the documents (TODO: if file .h then automatically new doc)
    val documentList = preprocessor.extractDocuments(fileContent)
    val filenameDoc = "outputSandbox/lexerDocs/$filename"
    for(i in documentList.indices) {
        // create the path
        val docFile = File("${filenameDoc}_doc$i.txt")
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

        val tokens = preprocessor.tokenize(fileContent)
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
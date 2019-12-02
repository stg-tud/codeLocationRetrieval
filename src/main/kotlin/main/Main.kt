package main

import preprocessor.TokenType
import preprocessor.Preprocessor
import java.io.File

fun main(args: Array<String>) {
    val preprocessor = Preprocessor()

    val filename = "probe"
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
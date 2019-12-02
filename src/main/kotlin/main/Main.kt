package main

import lexer.TokenType
import preprocessor.Preprocessor
import java.io.File

fun main(args: Array<String>) {
    val filename = "probe"
    val file = File("inputSandbox/$filename.c")

    val tokens = Preprocessor.tokenize(file)
    tokens.forEach {
        println(it)
    }

    // write the corpus
    val corpus = File("outputSandbox/corpus.txt").bufferedWriter()
    tokens.toSet().forEach {
        if(it.tokenType == TokenType.IDENTIFIER) {
            for(id in Preprocessor.getModifiedIdentifierList(it.value)) {
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
    val documentList = Preprocessor.extractDocuments(file.readText())
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
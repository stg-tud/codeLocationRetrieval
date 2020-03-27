package main

import main.console.ConsoleApplication
import main.console.LsiConsole
import main.console.VsmConsole
import java.io.File
import org.apache.commons.math3.linear.RealMatrix
import preprocessor.*
import java.lang.Exception
import java.util.*

private val mDocuments = ArrayList<Document>()
private val mTerms = HashSet<String>()

fun main(args: Array<String>) {
    println("ARGS = ${args.toList()}")
    Options.parse(args)
    processInput()

    val consoleApp = when(Options.irModel) {
        "lsi" -> LsiConsole(mTerms, mDocuments)
        "vsm" -> VsmConsole(mTerms, mDocuments)
        else -> object : ConsoleApplication(mTerms, mDocuments) {
            override fun start() {
                println("Could not find an appropriate console for argument ${Options.irModel}. Abort.")
                System.exit(64)
            }
        }
    }

    consoleApp.start()
}

private fun processInput() {
    val start = System.currentTimeMillis()

    val (terms, documents) = getTermsAndDocuments(inputRootDir = Options.inputRootDirectory, stopList = Options.stopList)

    if(documents.isEmpty()) {
        println("No C files were found. Please choose a directory that contains C files.")
        System.exit(0)
    }

    mTerms.addAll(terms)
    mDocuments.addAll(documents)

    // write terms
    val termsFileWriter = Options.outputTermsFile.bufferedWriter()
    mTerms.forEach {
        termsFileWriter.write(it)
        termsFileWriter.newLine()
    }
    termsFileWriter.close()

    // write documents
    var docIndex = 0
    try {
        for(document in mDocuments) {
            // in output/corpus: doc#_origFileName_origExtension.cc
            val docFile = File("${Options.outputCorpusDir}" +
                    "/doc${docIndex}_${document.sourceFile.nameWithoutExtension}_${document.sourceFile.extension}.cc")
//            docFile.parentFile.mkdirs()
            val docWriter = docFile.bufferedWriter()
            docWriter.write(document.content)
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

// extension function for printing Commons Math RealMatrix
@Suppress("unused")
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
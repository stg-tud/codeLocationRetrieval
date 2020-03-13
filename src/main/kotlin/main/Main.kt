package main

import java.io.File
import termdocmatrix.TermDocumentMatrix
import org.apache.commons.math3.linear.RealMatrix
import preprocessor.*
import retrieval.Query
import retrieval.lsi.LatentSemanticIndexingModel
import java.lang.Exception
import java.util.*

val mDocuments = ArrayList<Block>()
val mTerms = HashSet<String>()
lateinit var mTdm: TermDocumentMatrix

fun main(args: Array<String>) {
    println("ARGS = ${args.toList()}")
    Options.parse(args)

    bigInput()
    createTdm()
    mainLoop()
}

private fun bigInput() {
    val start = System.currentTimeMillis()

    val (terms, documents) = getTermsAndBlocks(inputRootDir = Options.inputRootDirectory)

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
        for(block in mDocuments) {
            // in output/corpus: doc#_origFileName_origExtension.cc
            val docFile = File("${Options.outputCorpusDir}" +
                    "/doc${docIndex}_${block.sourceFile.nameWithoutExtension}_${block.sourceFile.extension}.cc")
//            docFile.parentFile.mkdirs()
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
    println("Time to create the corpus: ${(end - start) / 1000f}s")
}

private fun createTdm() {
    val startTime = System.currentTimeMillis()

    // -1 because of empty line at the end (get rid of that)
    val matrix = TermDocumentMatrix(mTerms, mDocuments)
    println("Number of terms = ${matrix.numOfTerms}")
    println("Number of documents = ${matrix.numOfDocs}")
    mTdm = Options.termWeightingStrategy.weightEntries(matrix)

    // time in seconds
    println("Time to create the TDM: ${(System.currentTimeMillis() - startTime) / 1000}s")
}

private fun mainLoop() {
    val startTime = System.currentTimeMillis()
    val lsiModel = LatentSemanticIndexingModel(mTdm)
    println("Time(SVD): ${(System.currentTimeMillis() - startTime) / 1000}s")

    println("dim(U): ${lsiModel.svd.u.rowDimension} x ${lsiModel.svd.u.columnDimension}")
    println("dim(S): ${lsiModel.svd.s.rowDimension} x ${lsiModel.svd.s.columnDimension}")
    println("dim(VT): ${lsiModel.svd.vt.rowDimension} x ${lsiModel.svd.vt.columnDimension}")

    println("max singular value = ${lsiModel.svd.singularValues.first()}")
    println("min singular value = ${lsiModel.svd.singularValues.last()}")
    println("rank = ${lsiModel.svd.rank},\tnorm = ${lsiModel.svd.norm},\tcn = ${lsiModel.svd.conditionNumber}," +
            "\ticn = ${lsiModel.svd.inverseConditionNumber}")

    val scanner = Scanner(System.`in`)
    val querySb = StringBuilder()
    var k: Int = -1

    // true on first iteration !
    var isNewK = true
    var isNewQuery = true

    while(true) {
        if(isNewQuery) {
            // read in user query
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

        if(isNewK) {
            // print the singular values
            val singularValues = lsiModel.svd.singularValues
            for(i in singularValues.indices) {
                if(i > 0 && (i % 4 == 0)) {
                    println()
                }

                print(String.format("%4d: %8.4f\t\t", i + 1, singularValues[i]))
            }

            // dimensionality reduction k in [1, S.rowDim]
            do {
                print("\nType in a value for k [1, ${lsiModel.svd.s.rowDimension}]: ")
                while(!scanner.hasNextInt()) {
                    print("Type in a value for k [1, ${lsiModel.svd.s.rowDimension }]: ")
                    scanner.next()
                }
                k = scanner.nextInt()
            } while(!(1 <= k && k <= lsiModel.svd.s.rowDimension))
        }

        val query = Query(querySb.toString())

        val results = lsiModel.retrieveDocuments(k, query)
        results.subList(0, Integer.min(results.size, 20)).forEach { println(it) }

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
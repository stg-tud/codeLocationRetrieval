package main

import termdocmatrix.weighting.*
import java.io.File

object Options {

    // ==================
    // == Option Names ==
    // ==================

    private const val OPTION_HELP_MESSAGE       = "-h"
    private const val OPTION_WEIGHTING_STRATEGY = "--weighting-strategy"
    private const val OPTION_SVD_FILENAME       = "--svd-filename"
    private const val OPTION_ROOT_DIRECTORY     = "--root-directory"

    // ===================
    // == Option Values ==
    // ===================

    lateinit var termWeightingStrategy: TermWeightingStrategy
        private set

    // TODO: a bit dangerous, because could be paired with incompatible weighting strategy
    lateinit var svdFilename: String
        private set

    lateinit var inputRootDirectory: File
        private set

    // ==================================
    // == Output Directories And Files ==
    // ==================================

    lateinit var outputRootDir: File
        private set

    lateinit var outputCorpusDir: File
        private set

    lateinit var outputDecompositionsDir: File
        private set

    lateinit var outputTermsFile: File
        private set

    lateinit var outputSvdFile: File
        private set

    // =============
    // == Methods ==
    // =============

    // important: must be called before accessing any of the fields
    fun parse(args: Array<String>) {
        if(args.isEmpty() || args[0].isEmpty()) {
            setDefaultOptionsIfNecessary()
            createOutputDirectoriesAndFiles()
            printOptionsConfirmationMessage()
            return
        }

        // split the arguments at blanks
        val myArgs = args[0].split("""\s+""".toRegex())
        myArgs.forEach {
            var option = it
            var optionValue = ""

            if(option.contains("=")) {
                val split = option.split("=".toRegex())
                option = split[0]       // e.g. "--weighting-strategy"
                optionValue = split[1]  // e.g. "tf-idf"
            }

            when(option) {
                OPTION_HELP_MESSAGE -> {
                    printHelpMessage()
                    System.exit(0)
                }
                OPTION_WEIGHTING_STRATEGY -> {
                    termWeightingStrategy = weightingStrategy(optionValue)
                }
                OPTION_SVD_FILENAME -> {
                    // TODO: make sure it's a valid file name
                    svdFilename = optionValue
                }
                OPTION_ROOT_DIRECTORY -> {
                    // TODO: make sure optionValue contains a valid path
                    inputRootDirectory = File(optionValue)
                }
                else -> println("Some unknown option: $option, $optionValue")
            }
        }

        // Provide values for which no arguments were provided
        setDefaultOptionsIfNecessary()

        createOutputDirectoriesAndFiles()

        printOptionsConfirmationMessage()
    }

    private fun setDefaultOptionsIfNecessary() {
        // For those fields that haven't been initialized yet, provide default values
        if(!this::termWeightingStrategy.isInitialized) {
            termWeightingStrategy = LogEntropyWeighting()
        }

        if(!this::svdFilename.isInitialized) {
            svdFilename = "svd_${termWeightingStrategy.javaClass.simpleName}"
        }

        if(!this::inputRootDirectory.isInitialized) {
            inputRootDirectory = File("inputBig/grbl")
        }
    }

    private fun weightingStrategy(strategyName: String): TermWeightingStrategy {
        return when(strategyName) {
            "binary"        -> LocalBinaryWeighting()
            "tf"            -> TermFrequencyWeighting()
            "tf-idf"        -> TfIdfWeighting()
            "log-entropy"   -> LogEntropyWeighting()
            // throw Exception or settle for default (Log-Entropy)?
            else            -> throw RuntimeException("Unknown value for $OPTION_WEIGHTING_STRATEGY." +
                    "Expected one of 'binary', 'tf', 'tf-idf', 'log-entropy'. " +
                    "But was $strategyName")
        }
    }

    private fun printHelpMessage() {
        printFormattedOption(OPTION_HELP_MESSAGE, "Show possible options")
        printFormattedOption(OPTION_WEIGHTING_STRATEGY, "Specifies how to weight the entries of " +
                "the term-document matrix. Values are 'binary', 'tf', 'tf-idf', 'log-entropy' (default)")
        printFormattedOption(OPTION_SVD_FILENAME, "The SVD will be stored as a *.ser file " +
                "with the provided file name. By default, the file name will mirror the options in order to make it " +
                "identifiable. If the file already exists, it will be used to load the SVD from the *.ser file.")
        printFormattedOption(OPTION_ROOT_DIRECTORY, "The root directory where the C project is located at. " +
                "Can be an absolute path or a relative one. (Fragile option, currently defaults to 'inputBig/grbl')")
    }

    private fun printFormattedOption(option: String, description: String) {
        println(String.format("%-25s %s", option, description))
    }

    private fun printOptionsConfirmationMessage() {
        println("OPTIONS")
        println("\tTerm weighting strategy: ${termWeightingStrategy.javaClass.simpleName}")
        println("\tSVD file name (.ser):    $svdFilename")
        println("\tRoot directory:          $inputRootDirectory")
    }

    private fun createOutputDirectoriesAndFiles() {
        // assumes options have been set
        val outputRootDirPath = "output/${inputRootDirectory.name}"
        outputRootDir = File(outputRootDirPath)
        if(!outputRootDir.exists()) {
            outputRootDir.mkdirs()
        }

        outputCorpusDir = File("$outputRootDirPath/corpus")
        if(!outputCorpusDir.exists()) {
            outputCorpusDir.mkdirs()
        }

        outputDecompositionsDir = File("$outputRootDirPath/decompositions")
        if(!outputDecompositionsDir.exists()) {
            outputDecompositionsDir.mkdirs()
        }

        outputTermsFile = File("$outputRootDirPath/terms.txt")

        outputSvdFile = File("${outputDecompositionsDir.path}/$svdFilename.ser")
    }
}
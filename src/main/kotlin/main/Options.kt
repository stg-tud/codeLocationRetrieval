package main

import termdocmatrix.weighting.*

object Options {

    // ===================
    // == Option Values ==
    // ===================

    lateinit var termWeightingStrategy: TermWeightingStrategy
        private set

    // TODO: a bit dangerous, because could be paired with incompatible weighting strategy
    lateinit var svdFilename: String
        private set

    // ==================
    // == Option Names ==
    // ==================

    private const val OPTION_HELP_MESSAGE       = "-h"
    private const val OPTION_WEIGHTING_STRATEGY = "--weighting-strategy"
    private const val OPTION_SVD_FILENAME       = "--svd-filename"

    // important: must be called before accessing any of the fields
    fun parse(args: Array<String>) {
        if(args.isEmpty() || args[0].isEmpty()) {
            setDefaultOptionsIfNecessary()
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
                else -> println("Some unknown option: $option, $optionValue")
            }
        }

        // Provide values for which no arguments were provided
        setDefaultOptionsIfNecessary()

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
    }

    private fun printFormattedOption(option: String, description: String) {
        println(String.format("%-25s %s", option, description))
    }

    private fun printOptionsConfirmationMessage() {
        println("OPTIONS")
        println("\tTerm weighting strategy: ${termWeightingStrategy.javaClass.simpleName}")
        println("\tSVD file name (.ser):    $svdFilename")
    }
}
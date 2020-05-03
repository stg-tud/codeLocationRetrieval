package main

import com.beust.jcommander.IParameterValidator
import com.beust.jcommander.IStringConverter
import com.beust.jcommander.ParameterException
import termdocmatrix.weighting.*
import java.io.File

/**
 * Validates that the passed in IR model is supported. If not, a [ParameterException] is thrown.
 */
class SupportedIrModels : IParameterValidator {
    override fun validate(parameterName: String?, irModel: String?) {
        if(!Options.supportedIrModels.contains(irModel)) {
            throw ParameterException("The passed in model \'$irModel\' is not supported. The list of currently " +
                    "supported models is ${Options.supportedIrModels}")
        }
    }
}

/**
 * Validates that the passed in weighting strategy is supported. If not, a [ParameterException] is thrown.
 */
class SupportedWeightingStrategies : IParameterValidator {
    override fun validate(parameterName: String?, weightingStrategy: String?) {
        if(!Options.supportedWeightingStrategies.contains(weightingStrategy)) {
            throw ParameterException("The passed in weighting strategy \'$weightingStrategy\' is not supported. The list " +
                    "of currently supported models is ${Options.supportedWeightingStrategies}")
        }
    }
}

/**
 * Creates the appropriate [TermWeightingStrategy] object given the command line argument for the term weighting strategy.
 */
class TermWeightingConverter : IStringConverter<TermWeightingStrategy> {
    override fun convert(strategyName: String?): TermWeightingStrategy {
        return when(strategyName) {
            "binary"        -> LocalBinaryWeighting()
            "tf"            -> TermFrequencyWeighting()
            "tf-idf"        -> TfIdfWeighting()
            "log-entropy"   -> LogEntropyWeighting()
            // Perhaps you forgot to add the case for a new weighting scheme?
            else            -> throw RuntimeException("Unknown value for --weighting-strategy." +
                    "Expected one of 'binary', 'tf', 'tf-idf', 'log-entropy'. " +
                    "But was $strategyName")
        }
    }
}

/**
 * Creates a [File] object given a command line argument for the name of the file.
 */
class FileConverter : IStringConverter<File> {
    override fun convert(fileName: String?): File {
        return File(fileName)
    }
}

/**
 * Returns a list of [String] which make up the stop-list. If 'empty' is passed as the command line argument,
 * an empty list is returned. Otherwise a file name/path is expected and the contents of that file are split at whitespace
 * and returned as a list.
 */
class StopListConverter : IStringConverter<List<String>> {
    override fun convert(value: String?): List<String> {
        return if(value == null || value == "empty") {
            emptyList()
        }
        else {
            // split at whitespace
            File(value).readText().split("""\s+""".toRegex())
        }
    }
}
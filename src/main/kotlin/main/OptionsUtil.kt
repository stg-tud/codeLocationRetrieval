package main

import com.beust.jcommander.IParameterValidator
import com.beust.jcommander.IStringConverter
import com.beust.jcommander.ParameterException
import termdocmatrix.weighting.*
import java.io.File

class SupportedIrModel : IParameterValidator {
    override fun validate(parameterName: String?, irModel: String?) {
        val supportedIrModels = listOf("lsi", "vsm")
        if(!supportedIrModels.contains(irModel)) {
            throw ParameterException("The passed in model \'$irModel\' is not supported. The list of currently " +
                    "supported models is $supportedIrModels")
        }
    }
}

class SupportedWeightingStrategies : IParameterValidator {
    override fun validate(parameterName: String?, weightingStrategy: String?) {
        val supportedWeightingStrategies = listOf("binary", "tf", "tf-idf", "log-entropy")
        if(!supportedWeightingStrategies.contains(weightingStrategy)) {
            throw ParameterException("The passed in weighting strategy \'$weightingStrategy\' is not supported. The list " +
                    "of currently supported models is $supportedWeightingStrategies")
        }
    }
}

class TermWeightingConverter : IStringConverter<TermWeightingStrategy> {
    override fun convert(strategyName: String?): TermWeightingStrategy {
        return when(strategyName) {
            "binary"        -> LocalBinaryWeighting()
            "tf"            -> TermFrequencyWeighting()
            "tf-idf"        -> TfIdfWeighting()
            "log-entropy"   -> LogEntropyWeighting()
            // throw Exception or settle for default (Log-Entropy)?
            else            -> throw RuntimeException("Unknown value for --weighting-strategy." +
                    "Expected one of 'binary', 'tf', 'tf-idf', 'log-entropy'. " +
                    "But was $strategyName")
        }
    }
}

class FileConverter : IStringConverter<File> {
    override fun convert(fileName: String?): File {
        return File(fileName)
    }
}

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
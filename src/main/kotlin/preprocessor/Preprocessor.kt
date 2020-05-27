package preprocessor

import java.io.File

class Preprocessor {

    /**
     * Returns the set of all terms and [documents][Document] for a given C project.
     *
     * @param[inputRootDir] The root directory of the C project
     * @return a pair consisting of the set of terms and the list of documents within the entire project
     */
    fun getTermsAndDocuments(
        inputRootDir: File,
        stopList: List<String> = emptyList()
    ): Pair<Set<String>, List<Document>> {
        val termSet = mutableSetOf<String>()
        val documents = mutableListOf<Document>()

        inputRootDir.walkTopDown().forEach {
            // only operate on .h and .c files
            if (!(it.extension == "h" || it.extension == "c")) {
                return@forEach  // mimics a continue
            }

            val sourceCode = it.readText()
            val tokens = extractTokens(sourceCode)

            // documents
            documents.addAll(extractDocuments(tokens, sourceFile = it))
        }

        // construct the terms of the TDM based on the indexed documents
        documents.forEach { document ->
            termSet.addAll(document.terms)
        }

        termSet.removeAll(stopList)

        /* Possible place for stemming */

        return Pair(termSet, documents)
    }

    private fun extractDocuments(tokens: List<Token>, sourceFile: File): List<Document> {
        return Parser(tokens, sourceFile).parse()
    }

    private fun extractTokens(input: String): List<Token> {
        return Lexer(input).scan()
    }
}
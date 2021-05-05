package preprocessor

import java.io.File

import kotlin.streams.toList

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
    ): Pair<Set<Term>, List<Document>> {
        val termSet = mutableMapOf<String, MutableSet<Location>>()
        val termSetS = mutableSetOf<String>()
        val documents = mutableListOf<Document>()

        val preprocessor = Preprocessor()
        inputRootDir.walkTopDown().forEach {
            // only operate on .h and .c files

            val fileKind = when (it.extension) {
                "h" -> FileKind.Header
                "c" -> FileKind.Source
                else ->
                    return@forEach  // mimics a continue
            }

            val sourceCode = it.readText()
            val tokens = preprocessor.extractTokens(sourceCode, it.name, fileKind)

            // documents
            documents.addAll(preprocessor.extractDocuments(tokens, sourceFile = it))
        }

        // construct the terms of the TDM based on the indexed documents
        documents.forEach { document ->
            val terms = document.terms
            terms.forEach { t ->
                termSet.getOrPut(t.term, { mutableSetOf() }).addAll(t.locations)
                termSetS.add(t.term)
            }

        }

        val filteredSet = termSet.filter { !stopList.contains(it.key) }.map { Term(it.key, it.value) }.toSet()
        val sorted = filteredSet.stream().sorted { term, term2 -> term.term.compareTo(term2.term) }.toList()
        return Pair(filteredSet, documents)
    }

    private fun extractDocuments(tokens: List<Token>, sourceFile: File): List<Document> {
        return Parser(tokens, sourceFile).parse()
    }

    private fun extractTokens(input: String, fileName: String, fileKind: FileKind): List<Token> {
        return Lexer(input, fileName, fileKind).scan()
    }
}
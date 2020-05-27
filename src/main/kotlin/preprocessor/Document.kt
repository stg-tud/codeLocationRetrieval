package preprocessor

import java.io.File

/**
 * The representation of a document in the corpus.
 *
 * @param[content]          The actual source code.
 * @param[idsAndComments]   List of identifier and comment tokens that are part of this document.
 * @param[sourceFile]       The original file to which this document's content belongs to.
 */
data class Document(val content: String, val idsAndComments: List<Token>, val sourceFile: File) {
    //    val terms = extractTerms(idsAndComments, sourceFile)
    val terms = extractTerms(idsAndComments)
}

data class Term(val term: String, val locations: Set<Location>)

data class Corpus(val Blocks: List<Document>, val terms: List<Term>)

enum class FileKind {
    Header,
    Source
}

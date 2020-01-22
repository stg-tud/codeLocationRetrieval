package preprocessor

import java.io.File

/**
 * A function or declaration block.
 *
 * @param[content]          The actual source code.
 * @param[idsAndComments]   List of identifier and comment tokens that are part of this block.
 * @param[sourceFile]       The original file to which this block of code belongs to.
 */
data class Block(val content: String, val idsAndComments: List<Token>, val sourceFile: File) {
    val terms = extractTerms(idsAndComments)
}
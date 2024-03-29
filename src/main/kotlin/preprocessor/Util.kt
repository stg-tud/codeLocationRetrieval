package preprocessor

import preprocessor.TokenType.COMMENT
import preprocessor.TokenType.IDENTIFIER
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * Extracts a list of terms, given a list of tokens. If tokens has no IDENTIFIER or COMMENT, an empty list is returned.
 *
 * For example, when the input list consists of
 *      - an identifier: my_identifier
 *      - a comment: // This is a line comment
 * then the output list will be:
 *      [my_identifier, my, identifier, this, is, a, line, comment]
 */
fun extractTerms(tokens: List<Token>): List<Term> {
    val terms = HashMap<String, Set<Location>>()
    fun addTerm(t: String, l: Location) {
        terms[t] = terms[t].orEmpty() + l
    }


    for (token in tokens) {
        when (token.tokenType) {
            IDENTIFIER -> {
                addTerm(token.value.lowercase(), token.location)
                val modifiedTerm = getModifiedIdentifier(token.value)
                modifiedTerm?.split(" ")?.forEach { t ->
                    addTerm(t, token.location.withMeta(TokenMetaType.Kind, Kind.Identifier))
                }
            }
            COMMENT -> {
                extractTermsOutOfComment(token.value).forEach {
                    addTerm(it, token.location.withMeta(TokenMetaType.Kind, Kind.Comment))
                }

            }
            else -> { /* do nothing */
            }
        }
    }

    return terms.map { (t, l) -> Term(t, l) }
}

// e.g. camelCase       -> camel case
// e.g. with_underscore -> with underscore
// e.g. some_mixedCase  -> some mixed case
fun getModifiedIdentifier(identifier: String): String? {
    val hasUnderscore = identifier.contains("_")
    val hasCamelCase =
        identifier.uppercase(Locale.getDefault()) != identifier && identifier.lowercase(Locale.getDefault()) != identifier

    if (!hasUnderscore && !hasCamelCase) {
        // no underscore and no camel case -> nothing to do
        return null
    }

    val separateCamelCaseSb = StringBuilder()
    if (hasCamelCase) {
        // separate at appropriate positions
        val rangeLimit = identifier.indices.endInclusive
        for (i in 0..(rangeLimit - 1)) {
            val current = identifier[i]
            val next = identifier[i + 1]

            separateCamelCaseSb.append(current)
            if (current.isLowerCase() && next.isUpperCase()) {
                separateCamelCaseSb.append(" ")
            }
            // case for e.g. URILocation (I is upper, L is upper, but o is lower
            // so we've appended 'I' at this point, now put a space in-between I and L
            else if (i + 2 <= rangeLimit && current.isUpperCase() && next.isUpperCase()
                && identifier[i + 2].isLowerCase()
            ) {
                separateCamelCaseSb.append(" ")
            }

            if (i == (rangeLimit - 1)) {
                separateCamelCaseSb.append(next)
            }
        }
    } else {
        // no camel case -> just take the identifier as is
        separateCamelCaseSb.append(identifier)
    }

    var modifiedIdentifier = separateCamelCaseSb.toString().lowercase(Locale.getDefault())
    if (hasUnderscore) {
        modifiedIdentifier = modifiedIdentifier.replace('_', ' ')
    }

    if (!modifiedIdentifier.contains("""\s+""".toRegex())) {
        // no new words gained (e.g. can happen for Camel -> camel)
        return null
    }

    // also get rid of possible spaces at the beginning and/or end of the string
    return modifiedIdentifier.trim()
}

/**
 * Extracts a list of terms out of a (line or block) comment. The terms are all set to lowercase. E.g.
 *      Input: /* This is a camelCase within a Block_Comment */
 *      Output: [this, is, a, camelcase, within, a, block_comment]
 *
 * @param[comment] The comment out of which we want to extract terms. Is either block or line comment.
 * @return A list of all terms within the comment, all lowercase
 */
private fun extractTermsOutOfComment(comment: String): List<String> = CommentLexer(comment).scan()

// A simple lexer to extract identifiers out of a comment
private class CommentLexer(private val input: String) {
    private val terms = ArrayList<String>()
    private val termBuilder = StringBuilder()
    private var current = 0

    fun scan(): List<String> {
        while (!isAtEnd()) {
            scanChar()
        }

        return terms
    }

    fun scanChar() {
        val p = advance()

        if (p.isLetter()) {
            termBuilder.append(p)
            term()
        }
    }

    private fun term() {
        while (!isAtEnd() && (peek().isLetterOrDigit() || peek() == '_')) {
            termBuilder.append(advance())
        }
        val currentTerm = termBuilder.toString()

        terms.add(currentTerm.lowercase(Locale.getDefault()))
        val modifiedTerm = getModifiedIdentifier(currentTerm)
        if (modifiedTerm != null) {
            terms.addAll(modifiedTerm.split(" "))
        }

        termBuilder.setLength(0)
    }

    private fun advance() = input[current++]

    private fun peek() = input[current]

    private fun isAtEnd() = current >= input.length
}


package preprocessor

// a function or declaration block
data class Block(val content: String, val idsAndComments: List<Token>)
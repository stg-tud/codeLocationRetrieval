package preprocessor

import java.io.File

class Preprocessor {
    fun extractDocuments(tokens: List<Token>, sourceFile: File): List<Block> {
        return Parser(tokens, sourceFile).parse()
    }

    fun extractTokens(input: String): List<Token> {
        return Lexer(input).scan()
    }
}
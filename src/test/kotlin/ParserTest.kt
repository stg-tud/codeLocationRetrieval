import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import preprocessor.*
import java.io.File
import preprocessor.TokenType.*

class ParserTest {

    @Test
    fun testFileContents() {
        // Given input to the parser (maybe make a directory with multiple files instead of just one file)
        val sourceFile = File("src/test/resources/ParserTest/actualInput/planner.c")
        val parser = Parser(Lexer(sourceFile.readText()).scan(), sourceFile)

        // When parsing the input and retrieving the resulting documents
        val actualDocuments = parser.parse()

        // Then the contents should equal the expected output
        val expectedOutputDir = File("src/test/resources/ParserTest/expectedOutput/planner")
        expectedOutputDir.listFiles().forEachIndexed { index, expectedOutputFile ->
            if (expectedOutputFile.isFile) {
                // ignore whitespace (e.g. CRLF vs LF issues, empty spaces, etc. make this rather cumbersome)
                assertThat(actualDocuments[index].content).isEqualToIgnoringWhitespace(expectedOutputFile.readText())
            }
        }
    }

    @Test
    fun testBlockCount() {
        // Given input to the parser (maybe make a directory with multiple files instead of just one file)
        val sourceFile = File("src/test/resources/ParserTest/actualInput/planner.c")
        val parser = Parser(Lexer(sourceFile.readText()).scan(), sourceFile)

        // When parsing the input and retrieving the resulting documents
        val actualDocuments = parser.parse()

        // Then the block count should equal 13 (or should we say "number of files in expectedOutput directory?)
        assertThat(actualDocuments.size).isEqualTo(13)
    }

    @Test
    fun testIdsAndCommentsOnSmallDocument() {
        // Given input to the parser (maybe make a directory with multiple files instead of just one file)
        val sourceFile = File("src/test/resources/ParserTest/actualInput/planner.c")
        val parser = Parser(Lexer(sourceFile.readText()).scan(), sourceFile)

        // When parsing the input and retrieving a document of small size
        val actualDocuments = parser.parse()
        val smallDocument = actualDocuments[4]

        // Then a small document should contain the following number of identifiers and comments
        assertThat(smallDocument.idsAndComments).usingElementComparatorIgnoringFields("startIndex").containsExactly(
            Token(
                tokenType = IDENTIFIER, value = "plan_reset", startIndex = -1, location = Location(0, 0)
            ),
            Token(
                tokenType = IDENTIFIER, value = "memset", startIndex = -1, location = Location(0, 0)
            ),
            Token(
                tokenType = IDENTIFIER, value = "pl", startIndex = -1, location = Location(0, 0)
            ),
            Token(
                tokenType = IDENTIFIER, value = "planner_t", startIndex = -1, location = Location(0, 0)
            ),
            Token(
                tokenType = COMMENT, value = "// Clear planner struct", startIndex = -1, location = Location(0, 0)
            ),
            Token(
                tokenType = IDENTIFIER, value = "block_buffer_tail", startIndex = -1, location = Location(0, 0)
            ),
            Token(
                tokenType = IDENTIFIER, value = "block_buffer_head", startIndex = -1, location = Location(0, 0)
            ),
            Token(
                tokenType = COMMENT, value = "// Empty = tail", startIndex = -1, location = Location(0, 0)
            ),
            Token(
                tokenType = IDENTIFIER, value = "next_buffer_head", startIndex = -1, location = Location(0, 0)
            ),
            Token(
                tokenType = COMMENT,
                value = "// plan_next_block_index(block_buffer_head)",
                startIndex = -1,
                location = Location(0, 0)
            ),
            Token(
                tokenType = IDENTIFIER, value = "block_buffer_planned", startIndex = -1, location = Location(0, 0)
            ),
            Token(
                tokenType = COMMENT, value = "// = block_buffer_tail;", startIndex = -1, location = Location(0, 0)
            )
        )
    }

    @Test
    fun testTermsOnSmallDocument() {
        // Given input to the parser (maybe make a directory with multiple files instead of just one file)
        val sourceFile = File("src/test/resources/ParserTest/actualInput/planner.c")
        val parser = Parser(Lexer(sourceFile.readText()).scan(), sourceFile)

        // When parsing the input and retrieving a document of small size
        val actualDocuments = parser.parse()
        val smallDocument = actualDocuments[4]  // plan_reset() { ... }

        println(smallDocument.terms)
        println("====")

        // Then the document should contain the following terms
        assertThat(smallDocument.terms.map { it.term }).containsExactly(
            // void plan_reset() {
            "plan_reset", "plan", "reset",
            // memset(&pl, 0, sizeof(planner_t)); // Clear planner struct
            "memset",
            "pl",
            "planner_t", "planner", "t",
            "clear", "planner", "struct",
            // block_buffer_tail = 0;
            "block_buffer_tail", "block", "buffer", "tail",
            // block_buffer_head = 0; // Empty = tail
            "block_buffer_head", "block", "buffer", "head",
            "empty", "tail",
            // next_buffer_head = 1; // plan_next_block_index(block_buffer_head)
            "next_buffer_head", "next", "buffer", "head",
            "plan_next_block_index", "plan", "next", "block", "index",
            "block_buffer_head", "block", "buffer", "head",
            // block_buffer_planned = 0; // = block_buffer_tail;
            "block_buffer_planned", "block", "buffer", "planned",
            "block_buffer_tail", "block", "buffer", "tail"
        )
    }

    @Test
    fun testEmptyBlock() {
        // Given input to the parser (maybe make a directory with multiple files instead of just one file)
        val sourceFile = File("src/test/resources/ParserTest/actualInput/empty_block.c")
        val parser = Parser(Lexer(sourceFile.readText()).scan(), sourceFile)

        // When parsing the input and retrieving the resulting documents
        val actualDocuments = parser.parse()

        // Then the contents should equal the expected output
        val expectedOutput =
            File("src/test/resources/ParserTest/expectedOutput/empty_block/doc00_empty_block_c.cc").readText()
        assertThat(actualDocuments[0].content).isEqualToIgnoringWhitespace(expectedOutput)
    }

    @Test
    fun testConditionalCompiling() {
        // Given input to the parser (maybe make a directory with multiple files instead of just one file)
        val sourceFile = File("src/test/resources/ParserTest/actualInput/conditional_compiling.c")
        val parser = Parser(Lexer(sourceFile.readText()).scan(), sourceFile)

        // When parsing the input and retrieving the resulting documents
        val actualDocuments = parser.parse()

        // Then the contents should equal the expected output
        val expectedOutputDir = File("src/test/resources/ParserTest/expectedOutput/conditional_compiling")
        expectedOutputDir.listFiles().forEachIndexed { index, expectedOutputFile ->
            if (expectedOutputFile.isFile) {
                // ignore whitespace (e.g. CRLF vs LF issues, empty spaces, etc. make this rather cumbersome)
                assertThat(actualDocuments[index].content).isEqualToIgnoringWhitespace(expectedOutputFile.readText())
            }
        }
    }

    @Test
    fun testErrorHandling() {
        // Given input to the parser (maybe make a directory with multiple files instead of just one file)
        val sourceFile = File("src/test/resources/ParserTest/actualInput/unbalanced_braces.c")
        val parser = Parser(Lexer(sourceFile.readText()).scan(), sourceFile)

        // When parsing the input and retrieving the resulting documents
        val actualDocuments = parser.parse()

        // Then the contents should equal the expected output
        val expectedOutputDir = File("src/test/resources/ParserTest/expectedOutput/unbalanced_braces")
        expectedOutputDir.listFiles().forEachIndexed { index, expectedOutputFile ->
            if (expectedOutputFile.isFile) {
                // ignore whitespace (e.g. CRLF vs LF issues, empty spaces, etc. make this rather cumbersome)
                assertThat(actualDocuments[index].content).isEqualToIgnoringWhitespace(expectedOutputFile.readText())
            }
        }
    }
}
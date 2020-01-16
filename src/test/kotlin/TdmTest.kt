import main.getCorpusAndBlocks
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TdmTest {
    lateinit var matrix: Matrix

    @BeforeEach
    fun init() {
        val (corpusSet, blocks) = getCorpusAndBlocks("src/test/resources/TermDocMatrixTest/in")
        matrix = Matrix(corpusSet.size, blocks.size)
        matrix.populateTdm(corpusSet, blocks)
    }

    @Test
    fun testSize() {
        /*
            faculty
            n
            fibonacci
         */
        Assertions.assertThat(matrix.numOfTerms).isEqualTo(3)

        /*
            faculty.c
            fibonacci.c
         */
        Assertions.assertThat(matrix.numOfDocs).isEqualTo(2)
    }

    @Test
    fun testTdmPopulation() {
        /*
                        faculty.c   fibonacci.c
            faculty         2,          0
            n               4,          5
            fibonacci       0,          3
         */
        val expectedTdm = Array(3) { Array(2){0} }
        expectedTdm[0] = intArrayOf(2, 0).toTypedArray()    // "faculty"
        expectedTdm[1] = intArrayOf(4, 5).toTypedArray()    // "n"
        expectedTdm[2] = intArrayOf(0, 3).toTypedArray()    // "fibonacci"

        // println(Arrays.deepToString(matrix.tdm))
        // println(Arrays.deepToString(expectedTdm))

        Assertions.assertThat(matrix.tdm).isEqualTo(expectedTdm)
    }
}
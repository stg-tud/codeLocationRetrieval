import preprocessor.getTermsAndBlocks
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TdmTest {
    lateinit var matrix: Matrix

    @BeforeEach
    fun init() {
        val (corpusSet, blocks) = getTermsAndBlocks("src/test/resources/TermDocMatrixTest/in")
        matrix = Matrix(corpusSet, blocks)
    }

    @Test
    fun testSize() {
        /*
            faculty.c:      calculates the faculty function for n   (6)
            fibonacci.c:    the fibonacci function                  (1)

            -> [Calculates, the, faculty, function, for, n, fibonacci] (7)
         */
        Assertions.assertThat(matrix.numOfTerms).isEqualTo(7)

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
            calculates          1,          0
            the                 1,          1
            faculty             3,          0
            function            1,          1
            for                 1,          0
            n                   6,          5
            fibonacci           0,          4

         */
        val expectedTdm = Array(7) { Array(2){0} }
        expectedTdm[0] = intArrayOf(1, 0).toTypedArray()    // "Calculates"
        expectedTdm[1] = intArrayOf(1, 1).toTypedArray()    // "the"
        expectedTdm[2] = intArrayOf(3, 0).toTypedArray()    // "faculty"
        expectedTdm[3] = intArrayOf(1, 1).toTypedArray()    // "function"
        expectedTdm[4] = intArrayOf(1, 0).toTypedArray()    // "for"
        expectedTdm[5] = intArrayOf(6, 5).toTypedArray()    // "n"
        expectedTdm[6] = intArrayOf(0, 4).toTypedArray()    // "fibonacci"

        // println(Arrays.deepToString(matrix.tdm))
        // println(Arrays.deepToString(expectedTdm))

        Assertions.assertThat(matrix.tdm).isEqualTo(expectedTdm)
    }
}
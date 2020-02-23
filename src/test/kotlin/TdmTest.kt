import matrix.Matrix
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
            fibonacci.c:    the fibonacci function                  (1 - "fibonacci" is the only new term)

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
        val expectedTdm = Array(7) { DoubleArray(2) {0.0} }
        expectedTdm[0] = doubleArrayOf(1.0, 0.0)    // "Calculates"
        expectedTdm[1] = doubleArrayOf(1.0, 1.0)    // "the"
        expectedTdm[2] = doubleArrayOf(3.0, 0.0)    // "faculty"
        expectedTdm[3] = doubleArrayOf(1.0, 1.0)    // "function"
        expectedTdm[4] = doubleArrayOf(1.0, 0.0)    // "for"
        expectedTdm[5] = doubleArrayOf(6.0, 5.0)    // "n"
        expectedTdm[6] = doubleArrayOf(0.0, 4.0)    // "fibonacci"

        // println(Arrays.deepToString(matrix.data))
        // println(Arrays.deepToString(expectedTdm))

        Assertions.assertThat(matrix.data).isEqualTo(expectedTdm)
    }
}
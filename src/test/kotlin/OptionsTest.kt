import main.Options
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import termdocmatrix.weighting.LocalBinaryWeighting
import termdocmatrix.weighting.LogEntropyWeighting
import termdocmatrix.weighting.TermFrequencyWeighting
import termdocmatrix.weighting.TfIdfWeighting

// Important: the tests are green when run individually, but some may fail if all tests are run.
// This is because Options is a singleton. E.g., if one test sets the value for Options.svdFilename,
// then value will not be reset in the next test, which may cause the test to fail.
//
// For now leave it. But maybe consider a different approach (i.e. do not make Options a singleton)
// Read: http://blog.davidehringer.com/testing/test-driven-development/unit-testing-singletons/
class OptionsTest {

    @Test
    fun testNoArgumentsPassed() {
        // Given no arguments
        val args = arrayOf("")

        // When parsing options
        Options.parse(args)

        // Then default values should be set
        assertThat(Options.termWeightingStrategy).isInstanceOf(LogEntropyWeighting::class.java)
        assertThat(Options.svdFilename).isEqualTo("svd_LogEntropyWeighting_defaultStopList")
    }

/*
    @Test
    fun testPrintHelpMethodCalled() {
        // TODO: how to do this? Is it even possible to verify that printHelpMessage() was called?
        // Mock the SUT to check if correct methods were called
//        val options = spyk<Options>(recordPrivateCalls = true)

//        // This is used for Kotlin Singletons
//        mockkObject(Options, recordPrivateCalls = true)
//
//        // Given arguments to print help message
//        val args = arrayOf("-h")
//
//        // When parsing options
//        Options.parse(args)
//
////        verify { Options["printHelpMessage"] }
    }
*/

    @Test
    fun testSvdFilename() {
        // Given
        val args = arrayOf("--svd-filename=my_svd_name")

        // When
        Options.parse(args)

        // Then
        assertThat(Options.svdFilename).isEqualTo("my_svd_name")
    }

    @Nested
    inner class PassOnlyWeightingStrategy {
        @Test
        fun testWithBinaryWeighting() {
            // Given
            val args = arrayOf("--weighting-strategy=binary")

            // When
            Options.parse(args)

            // Then
            assertThat(Options.termWeightingStrategy).isInstanceOf(LocalBinaryWeighting::class.java)
            assertThat(Options.svdFilename).isEqualTo("svd_LocalBinaryWeighting")
        }

        @Test
        fun testWithTermFrequencyWeighting() {
            // Given
            val args = arrayOf("--weighting-strategy=tf")

            // When
            Options.parse(args)

            // Then
            assertThat(Options.termWeightingStrategy).isInstanceOf(TermFrequencyWeighting::class.java)
            assertThat(Options.svdFilename).isEqualTo("svd_TermFrequencyWeighting")
        }

        @Test
        fun testWithTfIdfWeighting() {
            // Given
            val args = arrayOf("--weighting-strategy=tf-idf")

            // When
            Options.parse(args)

            // Then
            assertThat(Options.termWeightingStrategy).isInstanceOf(TfIdfWeighting::class.java)
            assertThat(Options.svdFilename).isEqualTo("svd_TfIdfWeighting")
        }

        @Test
        fun testWithLogEntropyWeighting() {
            // Given
            val args = arrayOf("--weighting-strategy=log-entropy")

            // When
            Options.parse(args)

            // Then
            assertThat(Options.termWeightingStrategy).isInstanceOf(LogEntropyWeighting::class.java)
            assertThat(Options.svdFilename).isEqualTo("svd_LogEntropyWeighting")
        }
    }

}
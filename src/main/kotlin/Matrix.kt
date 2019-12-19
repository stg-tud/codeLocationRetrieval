// N terms and M documents
data class Matrix(val numOfTerms: Int, val numOfDocs: Int) {
    // a NxM array, initialized to all zeros
    val tdm = Array(numOfTerms) { Array(numOfDocs) {0}}
}
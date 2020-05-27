package retrieval

data class RetrievalResult(val docIdx: Int, val similarityScore: Double, val sourceFileName: String = "") {

    override fun toString(): String {
        return String.format(
            "Document Index: %5d, \tSimilarity Score: %8.4f, \tFile Name: %s",
            docIdx, similarityScore, sourceFileName
        )
    }
}

data class Location(val line: Int, val col: Int) {
    override fun toString() = "($line, $col)"
}
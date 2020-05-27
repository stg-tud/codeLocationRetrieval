package main

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import termdocmatrix.weighting.LogEntropyWeighting
import termdocmatrix.weighting.TermWeightingStrategy
import java.io.File

@Parameters(separators = "=")
object Options {

    // write-once to ensure immutability
    private var hasBeenWrittenTo = false

    // on change, don't forget to update descriptions (cannot be used in descriptions since compile-time const is required for annotations)
    val supportedIrModels = listOf("vsm", "lsi")
    val supportedWeightingStrategies = listOf("binary", "tf", "tf-idf", "log-entropy")
    val supportedScoreFunctions = listOf("dot", "cosine")

    // ==================
    // == Option Names ==
    // ==================

    private const val OPTION_HELP_MESSAGE = "-h"
    private const val OPTION_WEIGHTING_STRATEGY = "--weighting-strategy"
    private const val OPTION_SVD_FILENAME = "--svd-filename"
    private const val OPTION_ROOT_DIRECTORY = "--root-directory"
    private const val OPTION_STOP_LIST = "--stop-list"
    private const val OPTION_IR_MODEL = "--ir-model"
    private const val OPTION_SCORE_FUNCTION = "--score-function"

    // =========================
    // == Option Descriptions ==
    // =========================

    private const val DESCRIPTION_HELP_MESSAGE =
        "Shows possible options. Program execution will stop after help message is shown."

    private const val DESCRIPTION_WEIGHTING_STRATEGY = "Specifies how to weight the entries of " +
            "the term-document matrix. Values are 'binary', 'tf', 'tf-idf', 'log-entropy'."

    private const val DESCRIPTION_SVD_FILENAME = "The SVD will be stored as a *.ser file " +
            "with the provided file name. By default, the file name will mirror the options in order to make it " +
            "identifiable. If the file already exists, it will be used to load the SVD from the *.ser file."

    private const val DESCRIPTION_ROOT_DIRECTORY = "The root directory where the C project is located at. " +
            "Can be an absolute path or a relative one."

    private const val DESCRIPTION_STOP_LIST = "The stop-list to apply. " +
            "Pass 'empty' as an argument to not use a stop-list."

    private const val DESCRIPTION_IR_MODEL = "The model to use to retrieve information. Currently supported models:  " +
            "'lsi', 'vsm'."

    private const val DESCRIPTION_SCORE_FUNCTION = "The score function to be used to compute similarity between " +
            "two vectors. Currently supported score functions: 'dot', 'cosine'."

    // ===================
    // == Option Values ==
    // ===================

    @Parameter(names = [OPTION_HELP_MESSAGE], description = DESCRIPTION_HELP_MESSAGE, help = true)
    private var isHelp = false

    @Parameter(
        names = [OPTION_WEIGHTING_STRATEGY], description = DESCRIPTION_WEIGHTING_STRATEGY,
        converter = TermWeightingConverter::class, validateWith = [SupportedWeightingStrategies::class]
    )
    lateinit var termWeightingStrategy: TermWeightingStrategy
        private set

    @Parameter(
        names = [OPTION_ROOT_DIRECTORY], description = DESCRIPTION_ROOT_DIRECTORY, converter = FileConverter::class,
        required = true
    )
    lateinit var inputRootDirectory: File
        private set

    @Parameter(
        names = [OPTION_STOP_LIST],
        description = DESCRIPTION_STOP_LIST,
        listConverter = StopListConverter::class
    )
    lateinit var stopList: List<String>
        private set

    @Parameter(names = [OPTION_IR_MODEL], description = DESCRIPTION_IR_MODEL, validateWith = [SupportedIrModels::class])
    lateinit var irModel: String
        private set

    @Parameter(
        names = [OPTION_SCORE_FUNCTION],
        description = DESCRIPTION_SCORE_FUNCTION,
        validateWith = [SupportedScoringFunctions::class]
    )
    lateinit var scoreFunctionName: String
        private set

    /** If user does not provide a custom name for the SVD file, then this parameter will not be initialized.
    In that case a default name will be constructed for [svdFilename]. */
    @Parameter(names = [OPTION_SVD_FILENAME], description = DESCRIPTION_SVD_FILENAME)
    private lateinit var customSvdFilename: String

    lateinit var svdFilename: String
        private set

    // ==================================
    // == Output Directories And Files ==
    // ==================================

    lateinit var outputRootDir: File
        private set

    lateinit var outputCorpusDir: File
        private set

    lateinit var outputDecompositionsDir: File
        private set

    lateinit var outputTermsFile: File
        private set

    lateinit var outputSvdFile: File
        private set

    // ==========
    // == Misc ==
    // ==========

    private val defaultStopList = listOf(
        "a",
        "able",
        "about",
        "above",
        "abst",
        "accordance",
        "according>>>>>>> develop",
        "accordingly",
        "across",
        "act",
        "actually",
        "added",
        "adj",
        "affected",
        "affecting",
        "affects",
        "after",
        "afterwards",
        "again",
        "against",
        "ah",
        "all",
        "almost",
        "alone",
        "along",
        "already",
        "also",
        "although",
        "always",
        "am",
        "among",
        "amongst",
        "an",
        "and",
        "announce",
        "another",
        "any",
        "anybody",
        "anyhow",
        "anymore",
        "anyone",
        "anything",
        "anyway",
        "anyways",
        "anywhere",
        "apparently",
        "approximately",
        "are",
        "aren",
        "arent",
        "arise",
        "around",
        "as",
        "aside",
        "ask",
        "asking",
        "at",
        "auth",
        "available",
        "away",
        "awfully",
        "b",
        "back",
        "be",
        "became",
        "because",
        "become",
        "becomes",
        "becoming",
        "been",
        "before",
        "beforehand",
        "begin",
        "beginning",
        "beginnings",
        "begins",
        "behind",
        "being",
        "believe",
        "below",
        "beside",
        "besides",
        "between",
        "beyond",
        "biol",
        "both",
        "brief",
        "briefly",
        "but",
        "by",
        "c",
        "ca",
        "came",
        "can",
        "cannot",
        "can't",
        "cause",
        "causes",
        "certain",
        "certainly",
        "co",
        "com",
        "come",
        "comes",
        "contain",
        "containing",
        "contains",
        "could",
        "couldnt",
        "d",
        "date",
        "did",
        "didn't",
        "different",
        "do",
        "does",
        "doesn't",
        "doing",
        "done",
        "don't",
        "down",
        "downwards",
        "due",
        "during",
        "e",
        "each",
        "ed",
        "edu",
        "effect",
        "eg",
        "eight",
        "eighty",
        "either",
        "else",
        "elsewhere",
        "end",
        "ending",
        "enough",
        "especially",
        "et",
        "et-al",
        "etc",
        "even",
        "ever",
        "every",
        "everybody",
        "everyone",
        "everything",
        "everywhere",
        "ex",
        "except",
        "f",
        "far",
        "few",
        "ff",
        "fifth",
        "first",
        "five",
        "fix",
        "followed",
        "following",
        "follows",
        "for",
        "former",
        "formerly",
        "forth",
        "found",
        "four",
        "from",
        "further",
        "furthermore",
        "g",
        "gave",
        "get",
        "gets",
        "getting",
        "give",
        "given",
        "gives",
        "giving",
        "go",
        "goes",
        "gone",
        "got",
        "gotten",
        "h",
        "had",
        "happens",
        "hardly",
        "has",
        "hasn't",
        "have",
        "haven't",
        "having",
        "he",
        "hed",
        "hence",
        "her",
        "here",
        "hereafter",
        "hereby",
        "herein",
        "heres",
        "hereupon",
        "hers",
        "herself",
        "hes",
        "hi",
        "hid",
        "him",
        "himself",
        "his",
        "hither",
        "home",
        "how",
        "howbeit",
        "however",
        "hundred",
        "i",
        "id",
        "ie",
        "if",
        "i'll",
        "im",
        "immediate",
        "immediately",
        "importance",
        "important",
        "in",
        "inc",
        "indeed",
        "index",
        "information",
        "instead",
        "into",
        "invention",
        "inward",
        "is",
        "isn't",
        "it",
        "itd",
        "it'll",
        "its",
        "itself",
        "i've",
        "j",
        "just",
        "k",
        "keep",
        "keeps",
        "kept",
        "kg",
        "km",
        "know",
        "known",
        "knows",
        "l",
        "largely",
        "last",
        "lately",
        "later",
        "latter",
        "latterly",
        "least",
        "less",
        "lest",
        "let",
        "lets",
        "like",
        "liked",
        "likely",
        "line",
        "little",
        "'ll",
        "look",
        "looking",
        "looks",
        "ltd",
        "m",
        "made",
        "mainly",
        "make",
        "makes",
        "many",
        "may",
        "maybe",
        "me",
        "mean",
        "means",
        "meantime",
        "meanwhile",
        "merely",
        "mg",
        "might",
        "million",
        "miss",
        "ml",
        "more",
        "moreover",
        "most",
        "mostly",
        "mr",
        "mrs",
        "much",
        "mug",
        "must",
        "my",
        "myself",
        "n",
        "na",
        "name",
        "namely",
        "nay",
        "nd",
        "near",
        "nearly",
        "necessarily",
        "necessary",
        "need",
        "needs",
        "neither",
        "never",
        "nevertheless",
        "new",
        "next",
        "nine",
        "ninety",
        "no",
        "nobody",
        "non",
        "none",
        "nonetheless",
        "noone",
        "nor",
        "normally",
        "nos",
        "not",
        "noted",
        "nothing",
        "now",
        "nowhere",
        "o",
        "obtain",
        "obtained",
        "obviously",
        "of",
        "off",
        "often",
        "oh",
        "ok",
        "okay",
        "old",
        "omitted",
        "on",
        "once",
        "one",
        "ones",
        "only",
        "onto",
        "or",
        "ord",
        "other",
        "others",
        "otherwise",
        "ought",
        "our",
        "ours",
        "ourselves",
        "out",
        "outside",
        "over",
        "overall",
        "owing",
        "own",
        "p",
        "page",
        "pages",
        "part",
        "particular",
        "particularly",
        "past",
        "per",
        "perhaps",
        "placed",
        "please",
        "plus",
        "poorly",
        "possible",
        "possibly",
        "potentially",
        "pp",
        "predominantly",
        "present",
        "previously",
        "primarily",
        "probably",
        "promptly",
        "proud",
        "provides",
        "put",
        "q",
        "que",
        "quickly",
        "quite",
        "qv",
        "r",
        "ran",
        "rather",
        "rd",
        "re",
        "readily",
        "really",
        "recent",
        "recently",
        "ref",
        "refs",
        "regarding",
        "regardless",
        "regards",
        "related",
        "relatively",
        "research",
        "respectively",
        "resulted",
        "resulting",
        "results",
        "right",
        "run",
        "s",
        "said",
        "same",
        "saw",
        "say",
        "saying",
        "says",
        "sec",
        "section",
        "see",
        "seeing",
        "seem",
        "seemed",
        "seeming",
        "seems",
        "seen",
        "self",
        "selves",
        "sent",
        "seven",
        "several",
        "shall",
        "she",
        "shed",
        "she'll",
        "shes",
        "should",
        "shouldn't",
        "show",
        "showed",
        "shown",
        "showns",
        "shows",
        "significant",
        "significantly",
        "similar",
        "similarly",
        "since",
        "six",
        "slightly",
        "so",
        "some",
        "somebody",
        "somehow",
        "someone",
        "somethan",
        "something",
        "sometime",
        "sometimes",
        "somewhat",
        "somewhere",
        "soon",
        "sorry",
        "specifically",
        "specified",
        "specify",
        "specifying",
        "still",
        "stop",
        "strongly",
        "sub",
        "substantially",
        "successfully",
        "such",
        "sufficiently",
        "suggest",
        "sup",
        "sure",
        "t",
        "take",
        "taken",
        "taking",
        "tell",
        "tends",
        "th",
        "than",
        "thank",
        "thanks",
        "thanx",
        "that",
        "that'll",
        "thats",
        "that've",
        "the",
        "their",
        "theirs",
        "them",
        "themselves",
        "then",
        "thence",
        "there",
        "thereafter",
        "thereby",
        "thered",
        "therefore",
        "therein",
        "there'll",
        "thereof",
        "therere",
        "theres",
        "thereto",
        "thereupon",
        "there've",
        "these",
        "they",
        "theyd",
        "they'll",
        "theyre",
        "they've",
        "think",
        "this",
        "those",
        "thou",
        "though",
        "thoughh",
        "thousand",
        "throug",
        "through",
        "throughout",
        "thru",
        "thus",
        "til",
        "tip",
        "to",
        "together",
        "too",
        "took",
        "toward",
        "towards",
        "tried",
        "tries",
        "truly",
        "try",
        "trying",
        "ts",
        "twice",
        "two",
        "u",
        "un",
        "under",
        "unfortunately",
        "unless",
        "unlike",
        "unlik>>>>>>> developely",
        "until",
        "unto",
        "up",
        "upon",
        "ups",
        "us",
        "use",
        "used",
        "useful",
        "usefully",
        "usefulness",
        "uses",
        "using",
        "usually",
        "v",
        "value",
        "various",
        "'ve",
        "very",
        "via",
        "viz",
        "vol",
        "vols",
        "vs",
        "w",
        "want",
        "wants",
        "was",
        "wasnt",
        "way",
        "we",
        "wed",
        "welcome",
        "we'll",
        "went",
        "were",
        "werent",
        "we've",
        "what",
        "whatever",
        "what'll",
        "whats",
        "when",
        "whence",
        "whenever",
        "where",
        "whereafter",
        "whereas",
        "whereby",
        "wherein",
        "wheres",
        "whereupon",
        "wherever",
        "whether",
        "which",
        "while",
        "whim",
        "whither",
        "who",
        "whod",
        "whoever",
        "whole",
        "who'll",
        "whom",
        "whomever",
        "whos",
        "whose",
        "why",
        "widely",
        "willing",
        "wish",
        "with",
        "within",
        "without",
        "wont",
        "words",
        "world",
        "would",
        "wouldnt",
        "www",
        "x",
        "y",
        "yes",
        "yet",
        "you",
        "youd",
        "you'll",
        "your",
        "youre",
        "yours",
        "yourself",
        "yourselves",
        "you've",
        "z",
        "zero"
    )

    private const val defaultStopListText = "defaultStopList"
    private const val emptyStopListText = "noStopList"
    private const val customStopListText = "customStopList"

    // =============
    // == Methods ==
    // =============

    init {
        setDefaultOptions()
    }

    fun parse(args: Array<String>) {
        if (hasBeenWrittenTo) {
            throw RuntimeException("Attempt to parse options more than once. Options is write-once, read-only.")
        }
        hasBeenWrittenTo = true

        if (args.isEmpty() || args[0].isEmpty()) {
            // Use the defaults
            createOutputDirectoriesAndFiles()
            printOptionsConfirmationMessage()
            return
        }


        val argsAsArray = args[0].trim().split("""\s+""".toRegex()).toTypedArray()
        val commander = JCommander.newBuilder()
            .programName("Feature Location")
            .addObject(this)
            .build()

        commander.parse(*argsAsArray)    // expects vararg, so use Kotlin's spread operator (*) on Array<String>

        if (isHelp) {
            commander.usage()
            System.exit(0)
        }

        checkSvdFilename()
        createOutputDirectoriesAndFiles()
        printOptionsConfirmationMessage()
    }

    private fun setDefaultOptions() {
        termWeightingStrategy = LogEntropyWeighting()
        stopList = defaultStopList
        svdFilename = "svd_${termWeightingStrategy.javaClass.simpleName}_$defaultStopListText"
        irModel = "lsi"
        scoreFunctionName = "cosine"
    }

    private fun checkSvdFilename() {
        // check whether to use custom SVD name (if provided) or default one
        svdFilename = if (this::customSvdFilename.isInitialized) {
            customSvdFilename
        } else {
            val stopListText = when {
                stopList.isEmpty() -> emptyStopListText
                stopList == defaultStopList -> defaultStopListText
                else -> customStopListText
            }

            "svd_${termWeightingStrategy.javaClass.simpleName}_$stopListText"
        }
    }

    private fun printOptionsConfirmationMessage() {
        println("OPTIONS")
        println("\tIR-Model:                $irModel")
        println("\tTerm weighting strategy: ${termWeightingStrategy.javaClass.simpleName}")
        if (irModel == "lsi") {
            println("\tSVD file name (.ser):    $svdFilename")
        }
        println("\tRoot directory:          $inputRootDirectory")
        println("\tStop-list:               ${stopList.subList(0, Integer.min(stopList.size, 7))} ...")
        println("\tSimilarity function:     $scoreFunctionName")
    }

    private fun createOutputDirectoriesAndFiles() {
        // assumes options have been set
        val outputRootDirPath = "output/${inputRootDirectory.name}"
        outputRootDir = File(outputRootDirPath)
        if (!outputRootDir.exists()) {
            outputRootDir.mkdirs()
        }

        outputCorpusDir = File("$outputRootDirPath/corpus")
        if (!outputCorpusDir.exists()) {
            outputCorpusDir.mkdirs()
        }

        outputDecompositionsDir = File("$outputRootDirPath/decompositions")
        if (!outputDecompositionsDir.exists()) {
            outputDecompositionsDir.mkdirs()
        }

        outputTermsFile = File("$outputRootDirPath/terms.txt")

        outputSvdFile = File("${outputDecompositionsDir.path}/$svdFilename.ser")
    }
}
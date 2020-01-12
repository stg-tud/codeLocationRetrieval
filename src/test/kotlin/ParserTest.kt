import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import preprocessor.Lexer
import preprocessor.Parser
import java.io.File

class ParserTest {
    private lateinit var parser: Parser
    private lateinit var parserFunctionHeaders: Parser

    private val qsortContent =
            "/* qsort: /* sort v[left]...v[right] into increasing order */" + System.lineSeparator() +
            "void qsort(char *v[], int i, int j)" + System.lineSeparator() +
            "{" + System.lineSeparator() +
            "    // comment this out to \"quick test\" strings: \"abc\" \"def\" 'a'" + System.lineSeparator() +
            "    int i, last;" + System.lineSeparator() +
            "    void swap(char *v[], int i, int j);" + System.lineSeparator() +
            "" + System.lineSeparator() +
            "    if(left >= right)   /* do nothing if array contains */" + System.lineSeparator() +
            "        return;         /* fewer than two elements */" + System.lineSeparator() +
            "" + System.lineSeparator() +
            "    swap(v, left, (left + right)/2);" + System.lineSeparator() +
            "    last = left;" + System.lineSeparator() +
            "    for(i = left+1; i <= right; i++)" + System.lineSeparator() +
            "        if(strcmp(v[i], v[left])  < 0)" + System.lineSeparator() +
            "            swap(v, ++last, i);" + System.lineSeparator() +
            "    swap(v, left, last);" + System.lineSeparator() +
            "" + System.lineSeparator() +
            "    qsort(v, left, last-1);" + System.lineSeparator() +
            "    qsort(v, last+1, right);" + System.lineSeparator() +
            "}"

    private val swapContent = "/* swap: interchange v[i] and v[j] */" + System.lineSeparator() +
            "void swap(char *v[], int i, int j)" + System.lineSeparator() +
            "{" + System.lineSeparator() +
            "    char *temp;" + System.lineSeparator() +
            "" + System.lineSeparator() +
            "    temp = v[i];" + System.lineSeparator() +
            "    v[i] = v[j];" + System.lineSeparator() +
            "    v[j] = *temp;" + System.lineSeparator() +
            "}"

    @BeforeEach
    fun initParser() {
        val source = File("src/test/resources/testInput/qsort.c").readText()
        parser = Parser(Lexer(source).scan(), source)

        val functionHeadersSource = File("src/test/resources/testInput/func_headers.c").readText()
        parserFunctionHeaders = Parser(Lexer(functionHeadersSource).scan(), functionHeadersSource)
    }

    @Test
    fun testBlockCount() {
        val blocks = parser.parse()
        Assertions.assertThat(blocks.size).isEqualTo(2)
    }

    @Test
    fun testIdsAndComments() {
        val blocks = parser.parse()

        val qsort = blocks[0].idsAndComments
        val swap = blocks[1].idsAndComments

        Assertions.assertThat(qsort.size).isEqualTo(49)
        Assertions.assertThat(swap.size).isEqualTo(16)
    }

    @Test
    fun testContent() {
        val blocks = parser.parse()
        Assertions.assertThat(blocks[0].content).isEqualTo(qsortContent)
        Assertions.assertThat(blocks[1].content).isEqualTo(swapContent)
    }


    // ===========================
    // == Test Function Headers ==
    // ===========================

    @Test
    fun testFunctionHeaders() {
        val blocks = parserFunctionHeaders.parse()

        Assertions.assertThat(blocks[0].content).isEqualTo(f1)
        Assertions.assertThat(blocks[1].content).isEqualTo(f2)
        Assertions.assertThat(blocks[2].content).isEqualTo(f3)
        Assertions.assertThat(blocks[3].content).isEqualTo(f4)
        Assertions.assertThat(blocks[4].content).isEqualTo(f5)
        Assertions.assertThat(blocks[5].content).isEqualTo(f6)
        Assertions.assertThat(blocks[6].content).isEqualTo(f7)
        Assertions.assertThat(blocks[7].content).isEqualTo(f8)
        Assertions.assertThat(blocks[8].content).isEqualTo(f9)
        Assertions.assertThat(blocks[9].content).isEqualTo(f10)
        Assertions.assertThat(blocks[10].content).isEqualTo(f11)
        Assertions.assertThat(blocks[11].content).isEqualTo(f12)
        Assertions.assertThat(blocks[12].content).isEqualTo(f13)
        Assertions.assertThat(blocks[13].content).isEqualTo(f14)
        Assertions.assertThat(blocks[14].content).isEqualTo(f15)
        Assertions.assertThat(blocks[15].content).isEqualTo(f16)
        Assertions.assertThat(blocks[16].content).isEqualTo(f17)
        Assertions.assertThat(blocks[17].content).isEqualTo(f18)
        Assertions.assertThat(blocks[18].content).isEqualTo(f19)
        Assertions.assertThat(blocks[19].content).isEqualTo(f20)
        Assertions.assertThat(blocks[20].content).isEqualTo(f21)
    }

    companion object FunctionDocs {
        val f1 = "// Function 1 - as short as possible" + System.lineSeparator() +
                "main()" + System.lineSeparator() +
                "{" + System.lineSeparator() +
                "    printf(\"hello, world!\");" + System.lineSeparator() +
                "}"

        val f2 = "/* Function #2 has a return type */" + System.lineSeparator() +
                "void copy()" + System.lineSeparator() +
                "{" + System.lineSeparator() +
                "    int i;" + System.lineSeparator() +
                "    extern char line[], longest[];" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "    i = 0;" + System.lineSeparator() +
                "    while((longest[i] = line[i]) != '\\0')" + System.lineSeparator() +
                "        ++i;" + System.lineSeparator() +
                "}"

        val f3 = "/*" + System.lineSeparator() +
                " * This is the third function." + System.lineSeparator() +
                " * It has a return type and some parameters as input." + System.lineSeparator() +
                " */" + System.lineSeparator() +
                "int power(int base, int n)" + System.lineSeparator() +
                "{" + System.lineSeparator() +
                "    int p;" + System.lineSeparator() +
                "    for(p = 1; n > 0; --n)" + System.lineSeparator() +
                "        p = p * base;" + System.lineSeparator() +
                "    return p;" + System.lineSeparator() +
                "}"

        val f4 = "// #4 has return type pointer" + System.lineSeparator() +
                "char *month_name(int n)" + System.lineSeparator() +
                "{" + System.lineSeparator() +
                "    static char *name[] = {" + System.lineSeparator() +
                "        \"Illegal month\"," + System.lineSeparator() +
                "        \"Jan\", \"Feb\", \"Mar\", \"Apr\", \"May\", \"Jun\"," + System.lineSeparator() +
                "        \"Jul\", \"Aug\", \"Sep\", \"Oct\", \"Nov\", \"Dec\"" + System.lineSeparator() +
                "    };" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "    return (n < 1 || n > 12) ? name[0] : name[n];" + System.lineSeparator() +
                "}"

        val f5 = "// #5 takes a pointer to a function as input" + System.lineSeparator() +
                "void qsort(void *v[], int left, int right, int (*comp)(void*, void*))" + System.lineSeparator() +
                "{" + System.lineSeparator() +
                "    int i, last;" + System.lineSeparator() +
                "    void swap(void *v[], int, int);" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "    if(left >= right)   /* do nothing if array contains */" + System.lineSeparator() +
                "        return;         /* fewer than two elements */" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "    swap(v, left, (left + right)/2);" + System.lineSeparator() +
                "    last = left;" + System.lineSeparator() +
                "    for(i = left+1; i <= right; i++)" + System.lineSeparator() +
                "        if((*comp)(v[i], v[left]) < 0)" + System.lineSeparator() +
                "            swap(v, ++last, i);" + System.lineSeparator() +
                "    swap(v, left, last);" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "    qsort(v, left, last-1, comp);" + System.lineSeparator() +
                "    qsort(v, last+1, right, comp);" + System.lineSeparator() +
                "}"

        val f6 = "// #6 returns a struct" + System.lineSeparator() +
                "struct point makepoint(int x, int y)" + System.lineSeparator() +
                "{" + System.lineSeparator() +
                "    struct point temp;" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "    temp.x = x;" + System.lineSeparator() +
                "    temp.y = y;" + System.lineSeparator() +
                "    return temp;" + System.lineSeparator() +
                "}"

        val f7 = "/* #7 is static */" + System.lineSeparator() +
                "static Header *morecore(unsigned nu)" + System.lineSeparator() +
                "{" + System.lineSeparator() +
                "    char *cp, *sbrk(int);" + System.lineSeparator() +
                "    Header *up;" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "    if(nu < NALLOC)" + System.lineSeparator() +
                "        nu = NALLOC;" + System.lineSeparator() +
                "    cp = sbrk(nu * sizeof(Header));" + System.lineSeparator() +
                "    if(cp == (char*)-1) /* no space at all */" + System.lineSeparator() +
                "        return NULL;" + System.lineSeparator() +
                "    up = (Header*)cp;" + System.lineSeparator() +
                "    up->s.size = nu;" + System.lineSeparator() +
                "    free((void*)(up+1));" + System.lineSeparator() +
                "    return freep;" + System.lineSeparator() +
                "}"

        val f8 = "// #8 and onwards" + System.lineSeparator() +
                "short f8() { return -1; }"
        val f9 = "short int f9() { return -1; }"
        val f10 = "long f10() { return -1; }"
        val f11 = "long int f11() { return -1; }"
        val f12 = "unsigned f12() { return -1; }"
        val f13 = "unsigned int f13() { return -1; }"
        val f14 = "unsigned short f14() { return -1; }"
        val f15 = "unsigned short int f15() { return -1; }"
        val f16 = "unsigned long f16() { return -1; }"
        val f17 = "unsigned long int f17() { return -1; }"
        val f18 = "unsigned long long f18() { return -1; }"
        val f19 = "unsigned long long int f19() { return -1; }"
        val f20 = "long double f20() { return -1; }"

        val f21 = "void f21() {" + System.lineSeparator() +
                "#ifdef NAME" + System.lineSeparator() +
                "    if(isName) {}" + System.lineSeparator() +
                "#endif" + System.lineSeparator() +
                "        // ..." + System.lineSeparator() +
                "" + System.lineSeparator() +
                "    // Conditional compilation can cause some strange syntax to occur" + System.lineSeparator() +
                "#define A 2" + System.lineSeparator() +
                "#if A > 2" + System.lineSeparator() +
                "\tprintf(\"if%n\");" + System.lineSeparator() +
                "\tif(1 < 0) {" + System.lineSeparator() +
                "#elif A > 1" + System.lineSeparator() +
                "\tprintf(\"elif%n\");" + System.lineSeparator() +
                "\tif(2 > 1) {" + System.lineSeparator() +
                "#else" + System.lineSeparator() +
                "\tprintf(\"else%n\");" + System.lineSeparator() +
                "\tif(2 > 1) {" + System.lineSeparator() +
                "#endif" + System.lineSeparator() +
                "\t\tprintf(\"Hello World%n\");" + System.lineSeparator() +
                "\t}" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "    // can be more than just one { that's too much" + System.lineSeparator() +
                "#ifdef NAME" + System.lineSeparator() +
                "    if(someCondition) {" + System.lineSeparator() +
                "        doThis();" + System.lineSeparator() +
                "        if(someOtherCondition) {" + System.lineSeparator() +
                "#else" + System.lineSeparator() +
                "    if(someCondition) {" + System.lineSeparator() +
                "        doThat();" + System.lineSeparator() +
                "        if(someOtherCondition) {" + System.lineSeparator() +
                "#endif" + System.lineSeparator() +
                "            otherStuff();" + System.lineSeparator() +
                "        }" + System.lineSeparator() +
                "        thisAndThat();" + System.lineSeparator() +
                "     }" + System.lineSeparator() +
                "" + System.lineSeparator() +
                "    // nested pp-conditionals" + System.lineSeparator() +
                "#if A > 0" + System.lineSeparator() +
                "    if(isGreaterZero) {" + System.lineSeparator() +
                "        #ifdef NAME" + System.lineSeparator() +
                "            if(isDefined) {" + System.lineSeparator() +
                "        #else" + System.lineSeparator() +
                "            if(isOtherDefined) {" + System.lineSeparator() +
                "        #endif" + System.lineSeparator() +
                "                // ..." + System.lineSeparator() +
                "            }" + System.lineSeparator() +
                "#elif" + System.lineSeparator() +
                "    if(isAnything) {" + System.lineSeparator() +
                "#else" + System.lineSeparator() +
                "    if(isSomething) {" + System.lineSeparator() +
                "        #ifdef NAME" + System.lineSeparator() +
                "            if(isDefined) {" + System.lineSeparator() +
                "        #else" + System.lineSeparator() +
                "            if(isOtherDefined) {" + System.lineSeparator() +
                "        #endif" + System.lineSeparator() +
                "                // ..." + System.lineSeparator() +
                "            }" + System.lineSeparator() +
                "#endif" + System.lineSeparator() +
                "        // ..." + System.lineSeparator() +
                "    }" + System.lineSeparator() +
                "    printf(\"Done\");" + System.lineSeparator() +
                "}"
    }
}
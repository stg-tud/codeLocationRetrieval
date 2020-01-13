/*
 * Dangling comment.
 */

#include <stdio.h>

enum boolean { FALSE, TRUE };

// Close but still dangling - or should this be included? -> Include it after all
// Function 1 - as short as possible
main()
{
    printf("hello, world!");
}

/* Function #2 has a return type */
void copy()
{
    int i;
    extern char line[], longest[];

    i = 0;
    while((longest[i] = line[i]) != '\0')
        ++i;
}

/*
 * This is the third function.
 * It has a return type and some parameters as input.
 */
int power(int base, int n)
{
    int p;
    for(p = 1; n > 0; --n)
        p = p * base;
    return p;
}

// #4 has return type pointer
char *month_name(int n)
{
    static char *name[] = {
        "Illegal month",
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    return (n < 1 || n > 12) ? name[0] : name[n];
}

// #5 takes a pointer to a function as input
void qsort(void *v[], int left, int right, int (*comp)(void*, void*))
{
    int i, last;
    void swap(void *v[], int, int);

    if(left >= right)   /* do nothing if array contains */
        return;         /* fewer than two elements */

    swap(v, left, (left + right)/2);
    last = left;
    for(i = left+1; i <= right; i++)
        if((*comp)(v[i], v[left]) < 0)
            swap(v, ++last, i);
    swap(v, left, last);

    qsort(v, left, last-1, comp);
    qsort(v, last+1, right, comp);
}

// #6 returns a struct
struct point makepoint(int x, int y)
{
    struct point temp;

    temp.x = x;
    temp.y = y;
    return temp;
}

#define NALLOC 1024
/* #7 is static */
static Header *morecore(unsigned nu)
{
    char *cp, *sbrk(int);
    Header *up;

    if(nu < NALLOC)
        nu = NALLOC;
    cp = sbrk(nu * sizeof(Header));
    if(cp == (char*)-1) /* no space at all */
        return NULL;
    up = (Header*)cp;
    up->s.size = nu;
    free((void*)(up+1));
    return freep;
}

/*
 * Dangling comment, the next couple functions test these return types:
 * (from http://eel.is/c++draft/dcl.type.simple#tab:simple.type.specifiers)
 *
 * signed int
 * short
 * short int
 * long
 * long int
 * unsigned
 * unsigned int
 * unsigned short
 * unsigned short int
 * unsigned long
 * unsigned long int
 * unsigned long long
 * unsigned long long int
 * long double
 */

/* #8 and onwards */
short f8() { return -1; }
short int f9() { return -1; }
long f10() { return -1; }
long int f11() { return -1; }
unsigned f12() { return -1; }
unsigned int f13() { return -1; }
unsigned short f14() { return -1; }
unsigned short int f15() { return -1; }
#define NUISANCE "This is not part of a function." \
    "And multiline is not a problem, too" \
    MULTI_NUISANCE
unsigned long f16() { return -1; }
#
unsigned long int f17() { return -1; }
unsigned long long f18() { return -1; }
unsigned long long int f19() { return -1; }
long double f20() { return -1; }

void f21() {
#ifdef NAME
    if(isName) {}
#endif
        // ...

    // Conditional compilation can cause some strange syntax to occur
#define A 2
#if A > 2
	printf("if%n");
	if(1 < 0) {
#elif A > 1
	printf("elif%n");
	if(2 > 1) {
#else
	printf("else%n");
	if(2 > 1) {
#endif
		printf("Hello World%n");
	}

    // can be more than just one { that's too much
#ifdef NAME
    if(someCondition) {
        doThis();
        if(someOtherCondition) {
#else
    if(someCondition) {
        doThat();
        if(someOtherCondition) {
#endif
            otherStuff();
        }
        thisAndThat();
     }

    // nested pp-conditionals
#if A > 0
    if(isGreaterZero) {
        #ifdef NAME
            if(isDefined) {
        #else
            if(isOtherDefined) {
        #endif
                // ...
            }
#elif
    if(isAnything) {
#else
    if(isSomething) {
        #ifdef NAME
            if(isDefined) {
        #else
            if(isOtherDefined) {
        #endif
                // ...
            }
#endif
        // ...
    }
    printf("Done");
}

/* Dangling comment: now mix in some extern, static, const, and volatile */

// Also: test params: void with no parameter name, and varargs (i.e. ", ..." at the end)
// varargs: #include <stdarg.h> (https://jameshfisher.com/2016/11/23/c-varargs/)
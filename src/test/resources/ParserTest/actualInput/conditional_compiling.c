// Simple
#if SOME_CONDITION
    void foo() {
#else
    void bar() {
#endif
        int some = 0;
        int code = 1;
    }

// Nested
#if OUTER
    void foo(int param1,
    #if INNER
        param2) {
    #else
        param3) {
    #endif
#else
    void bar() {
#endif
        int some = 0;
        int code = 1;
    }

// (Simple) Brace after #endif
#if SOME_CONDITION
    void foo()
#else
    void bar()
#endif
    {
        int some = 0;
        int code = 1;
    }
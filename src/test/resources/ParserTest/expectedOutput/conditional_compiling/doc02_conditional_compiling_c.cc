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
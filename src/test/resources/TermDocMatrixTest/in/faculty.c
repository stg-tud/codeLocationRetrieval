unsigned int faculty(unsigned int n) {
    if(n == 0) {
        return 1;
    }
    else {
        return n * faculty(n - 1);
    }
}
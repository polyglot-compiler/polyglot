class BadFinalInit14 {
    final int N;
    BadFinalInit14() { N = 0; }
    class Inner {
        Inner() {
            N = 0; // error: trying to initialize field of outer class
        }
    }
}

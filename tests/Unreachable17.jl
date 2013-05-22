class C {
    final public static boolean FOO = true;
    void m() {
	int t;
	while (!FOO) {
	    t = 1; // unreachable
	}
	for ( ; !FOO; ) {
	    t = 1; //unreachable
	}
    }
}

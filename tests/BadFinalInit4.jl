class BadFinalInit4 {
    void foo(final int i) {
	if (false) {
	    i = 1; // bad: i might have already been assigned to, as it is a formal. However,
	           // according to the language spec, i is both definitely assigned and
	           // definitely unassigned, since this is dead code. So this should be allowed.
	}
    }
}

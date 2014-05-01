class T846i1 {
	private int m() { return 1; }
	static class Inner extends T846i1 {
		int j = m(); // m not inherited, and enclosing m not available
	}
}

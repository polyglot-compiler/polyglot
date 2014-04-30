class T83i1 {
	private int i;
	static class Inner extends T83i1 {
		int j = i; // i not inherited, and enclosing i not available
	}
}

class C {
	static void foo() {
		boolean x, y = true;
		if (false && y)
			y = x;
		if (true || y);
		else y = x;
	}
}

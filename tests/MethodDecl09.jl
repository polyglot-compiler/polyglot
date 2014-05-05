class T8463p1 {
	static class One {
		private final int m() { return 1; }
	}
	static class Two extends One {
		private static void m() {}
	}
	static class Three extends Two {
		Object m() { return null; }
	}
}

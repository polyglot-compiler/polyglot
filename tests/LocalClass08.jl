class C {
	public static void main(final String[] args) {
		class Local {
			static int i;
			static void foo() {}
			static {}
			static class Nested {}
			interface Inner {}
		}
	}
}

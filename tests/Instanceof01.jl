class T15202n4 {
	interface I { void m(); }
	interface J { int m(); }
	boolean n(I i) {
		return i instanceof J;
	}
}

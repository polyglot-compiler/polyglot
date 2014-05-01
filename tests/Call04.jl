class T151222a31 {
	interface I {
		String toString();
	}
	class A implements I {}
	class B extends A {
		{ super.toString(); }
	}
}

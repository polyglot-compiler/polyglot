class T92i13 {
	interface I { Object clone() throws java.io.IOException; }
	class C implements I {
		public Object clone() { return null; }
	}
}

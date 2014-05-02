class T85i2 {
	class C {}
	static class One extends T85i2 {
		private class C {} // T85i2.C not inherited...
	}
	static class Two extends One {
		Two.C c; // ...so neither C is inherited
	}
}

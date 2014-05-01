class T83i4 {
	int i;
	class One extends T83i4 {
		private int i; // T83i4.i not inherited...
	}
	class Two extends One {
		Two() {
			T83i4.this.super();
		}
		int j = this.i; // ...so neither i is inherited
	}
}

class C {
	void foo() {
		class Local1 {
			void foo() {
				class Local2 {
					void foo() {
						class Local3 {
						}
						new Local1();
						new Local2();
						new Local3();
					}
				}
				new Local1();
				new Local2().foo();
			}
		}
		new Local1().foo();
	}
}

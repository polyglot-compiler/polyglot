class T159u2 {
	class Inner {}
	static class Sinner {}
	void foo() {
		new T159u2(); // toplevel
		new Inner(); // member
		new Sinner(); // static member

		class Local {}
		new Local(); // local

		new T159u2() {}; // anonymous
	}
}

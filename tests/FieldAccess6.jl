class C {
	private Object o;
	void foo(D d) {
		d.o = null;
	}
}

class D extends C {
}

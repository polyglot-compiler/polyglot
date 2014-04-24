import package1.C;

class D extends C {
	D() {
		super(1);
	}

	class Inner extends C.Inner {
		Inner() {
			super("Hello");
		}
		C.Inner inner = new C.Inner("Hi");
	}
	
	C c = new C(0);
}

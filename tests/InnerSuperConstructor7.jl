class A {
    class B extends A {}
}

class AA extends A {
    class BB extends B {
	BB() {
	    super();
	}
    }
}

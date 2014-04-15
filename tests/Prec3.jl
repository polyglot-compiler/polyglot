class C {
    class D { 
	int foo() { return 3; }
    }

    void m(C x) {
	int t = (((C)x).new D()).foo();
    }
}

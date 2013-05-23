// According to the JLS 2nd edition, this method call is
// ambiguous, since neither A2.visit(B1) nor A1.visit(B2) 
// is more specific than the other accord to the JLS 2nd ed
// definition of "more specific". This is because the definition
// requires a method invocation conversion between the _containers_
// of the method. 
// This appears to be an error in the specification, as
// the call should resolved to A1.visit(B2). Indeed,
// in the third edition of the JLS the comparison of the 
// containers is dropped from the definition of "more
// specific".

class C  { 
    void m(B2 node) {
	new A2().visit(node);
    }
}

class A2 extends A1 {
    public void visit(B1 node) {   }
}

class A1 {
    public void visit(B2 node) {   }
}

class B2 extends B1 { }
class B1 { }


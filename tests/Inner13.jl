// This test case causes an infinite loop in disambiguation.
class B {
  class A {
    B n = new B() { A x; };
  }
}


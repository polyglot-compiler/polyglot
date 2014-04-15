class C {
  private void foo() {
  }
  void bar(D d) {
    d.foo();
  }
}

class D extends C {
}
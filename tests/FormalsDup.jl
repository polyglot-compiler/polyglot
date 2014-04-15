class FormalsDup {
  void m(int x, int x) { } // Error: x multiply defined
  void p(int x) { int x; } // Error: x multiply defined
  void n(int x) { class C { void q(int x) { } } } // OK
}

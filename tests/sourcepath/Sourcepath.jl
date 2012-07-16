class C {
  void foo() {
    // try to access a public method of a protected class, but via a public subclass 
    package1.PubClassInheritMeth x = null;
    x.m();
  }
}

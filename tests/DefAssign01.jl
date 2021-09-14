class C {
  final int x,y;

  // Only should show an error here about x, y not being initialized.
  { y = x; }

  C() {
    x = 0;
  }
}

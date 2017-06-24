class C {
  final int x,y;

  // Only should show an error here about x not being initialized.
  // Should not show an error about y not being initialized.
  { y = x; }

  C() {
    x = 0;
  }
}

class BadFinalInit18 {
  static class Super {
    final int x = 0;
  }

  static class Sub extends Super {
    Sub() {
      x = 0; // bad
    }
  }
}

package polyj.tests.bad.semant;

class V[T] {
  static int i = 10;
  static int j = 10;
  void m() {
    i++;
    j *= 3;
  }
}

// This tests whether we detect reachable code properly.

public class Assignment2 {
  // The following methods all have some
  // unreachable code.
  void m1() {
    int x;
    do {
      x = 1;
    } while (b);
    x++;
  }

  void m2() {
    int x;
    while (b) {
      x = 1;
    }
    x++;
  }

  int m3() {
    int x;
    for (;b;) {
      x = 1;
    }
    return x;
  }

  boolean b;

  int m4() {
    int x;
    for (x = 0; x < 10; x++) {
      x++;
    }
    return x;
  }
}

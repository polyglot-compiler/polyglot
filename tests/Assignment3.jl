// This tests whether we detect reachable code properly.

public class Assignment3 {
  // The following methods all have some
  // unreachable code.
  void m1() {
    int x;
    do {
      x = 1;
    } while (b);
    x++;
  }

  void m5() {
    int x;
    if ((x = 1) > 0) {
    }
      x++;
  }

  void m6() {
    int x;
    if ((x = 1) == x) {
    }
      x++;
  }

  final boolean d = true;
  final boolean b = d;

  /*
  void m2() {
    int x;
    while (true) {
      x = 1;
    }
    x++;
  }
  */

  /*
  int m3() {
    int x;
    for (;true;) {
      x = 1;
    }
    return x;
  }
  */

  /*
  int m4() {
    int x;
    for (;;) {
      x = 1;
    }
    return x;
  }
  */
}

// This tests whether we detect reachable code properly.

public class Assignment {
  // The following methods all have some
  // unreachable code.
  void m1() {
    int x;
    x = 1;
  }

  void m2() {
    int x;
    x++;
  }

  int m3() {
    int x;
    return x;
  }

  boolean b;

  int m4() {
    int x;
    if (b) x = 1;
    return x;
  }

  void m5() {
    int x;
    if (b) x = 1;
    else x = 2;
    x++;
  }

  void m6() {
    int x;
    if (b) x = 1;
    else return;
    x++;
  }

}

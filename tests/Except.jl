class Except {
  int x;
  Object m(A a) {
    try {
      a.x = 0;
    }
    catch (NullPointerException e) {
      return e;
    }
    return null;
  }
}


// This tests whether we detect reachable code properly.

public class Unreachable {
  // The following methods all have some
  // unreachable code.
  void m1() {
    return;
    System.out.println("Never happens."); // BAD (m1)
  }

  void m2() {
    throw new NullPointerException();
    System.out.println("Not likely."); // BAD (m2)
  }

  void m3a() {
    while (1==1) {
      if (1==2) {
	break;
      }
    }
    System.out.println("This one _should_ be legal.");
  }
  
  void m3() {
    while (true) {
      if (1==1) {
	break;
	return; // BAD (m3)
      }
    }
    System.out.println("This one _should_ be legal.");
  }
  
  void m4() {
    int i = 0;
    do {
      i++;
      continue;
      return; // BAD (m4)
    } while (i != 3);
  }

  void m5() {
  l1:
    while(true) {
    l2:
      while(true) {
	break l1;
      }
      System.out.println("No good"); // BAD. (m5)
    }
    System.out.println("Okay");
  }

  void m6() {
    int i = 4;
    switch (i) {
    case 3:
      break;
      System.out.println("no good"); // BAD (m6)
    case 4:
    default: 
      return;
    }
    System.out.println("okay.");
  }

  void m7() {
    int i = 4;
    switch (i) {
    case 1:
      throw new NullPointerException();      
    default:
      return;
    }
    System.out.println("no good"); // BAD (m7)
  }

  void m8() {
    try {
    } catch (Exception e) {}
    System.out.println("Just ducky."); 
    try { 
      return;
    } catch (Exception e) { return; }
    System.out.println("Won't happen."); // BAD (m8)
  }

  void m9() {
    try {
      System.out.println("hello world.");
    } finally {
      return;
    }
    System.out.println("Bad."); // BAD (m9)
  }

  void m10() {
    try {
      return;
    } finally {
      System.out.println("Okay");
    }
    System.out.println("Bad."); // BAD (m10)
  }

  void m11() {
      try {
        throw new NullPointerException();
      }
      finally {
      }
      System.out.println("Bad"); // BAD (m11)
  }

  void m11a() {
    try {
      throw new NullPointerException();
    }
    catch (Exception e) {}
    return; // Should be ok.
  }

  void m11b() {
    try {
      try {
        throw new NullPointerException();
      }
      finally {
      }
    }
    catch (Exception e) {}
    return; // Should be OK.
  }


  int m11c() {
      try {
         new NullPointerException();
      }
      catch (Exception e) {}
      return 1; // Should be OK.
  }
}

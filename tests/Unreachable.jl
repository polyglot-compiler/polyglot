package polyj.tests.bad.semant;

// This tests whether we detect reachable code properly.

public class Reachable {
  // The following methods all have some
  // unreachable code.
  void m1() {
    return;
    System.out.println("Never happens."); // BAD (10)
  }

  void m2() {
    throw new NullPointerException();
    System.out.println("Not likely."); // BAD (15)
  }

  void m3() {
    while (true) {
      if (1==1) {
	break;
	return; // BAD (22)
      }
    }
    System.out.println("This one _should_ be legal.");
  }
  
  void m4() {
    int i = 0;
    do {
      i++;
      continue;
      return; // BAD (33)
    } while (i != 3);
  }

  void m5() {
  l1:
    while(true) {
    l2:
      while(true) {
	break l1;
      }
      System.out.println("No good"); // BAD. (44)
    }
    System.out.println("Okay");
  }

  void m6() {
    int i = 4;
    switch (i) {
      System.out.println("ouch!"); // BAD (52)
    case 3:
      break;
      System.out.println("no good"); // BAD (55)
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
    System.out.println("no good"); // BAD (71)
  }

  void m8() {
    try {
      return;
    } catch (Exception e) {}
    System.out.println("Just ducky."); 
    try { 
      return;
    } catch (Exception e) { return; }
    System.out.println("Won't happen."); // BAD (82)
  }

  void m9() {
    try {
      System.out.println("hello world.");
    } finally {
      return;
    }
    System.out.println("Bad."); // BAD (91)
  }

  void m10() {
    try {
      return;
    } finally {
      System.out.println("Okay");
    }
    System.out.println("Bad."); // BAD (100)
  }

}

package polyj.tests.bad.semant;

// This tests whether we detect methods without return statements properly.

public class NoReturns {
  // The following methods all have some
  // unreachable code.
  int m1() {
  } // BAD

  int m2() {
    throw new NullPointerException();    
  }

  int m3() {
    while (true) {
      if (1==1) {
	break;
      }
    }
  } // BAD
  
  int m4() {
  l1:
    while(true) {
    l2:
      while(true) {
	break l1;
      }
    }
  } // BAD

  int m5() {
    int i = 5;
    switch (i) {
    case 3:
      return 4;
    case 4:
    default: 
      return 6;
    }
  }
  
  int m6() {
    int i = 5;
    switch (i) {
    case 3:
      return 4;
    case 4:
      return 6;
    }    
  } // BAD

  int m7() {
    try {
      return 7;
    } catch (Exception e) {}
  } // BAD

  int m8() {
    try {
      return 9;
    } finally {
      return 10;
    }
  }
}

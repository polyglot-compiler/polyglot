// This tests whether we detect variables which are used before they get
// initialized.

public class NoInit {
  void foo(int i) {}

  void m1() {
    int i;
    foo(i); // BAD
  } 

  void m2() {
    int i;
    while (true) {
      if (1==1) {
	break;
      }
      i = 3;
    }
    foo(i); // BAD
  } 
  
  void m3() {
    int i;
  l1:
    while(true) {
    l2:
      while(true) {
	break l1;
      }
      i = 3;
    }
    foo(i); // BAD
  } 

  void m4() {
    int i = 5;
    int j;
    switch (i) {
    case 3:
      j = 1;
      break;
    case 4:
    default: 
      j = 3;
      break;
    }
    foo(j);
  }
  
  void m5() {
    int i = 5;
    int j;
    switch (i) {
    case 3:
      j = 4;
      break;
    case 4:
      j = 6;
    }
    foo(j); // BAD
  }

  void m6() {
    int i;
    boolean b = true;
    if (b)
      i = 4;
    else 
      i = 7;
    foo(i);
  }

  void m7() {
    int i;
    boolean b = true;
    if (b)
      i = 7;
    foo(i); // BAD
  }
}


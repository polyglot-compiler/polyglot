public class InitCheckerBug {
  static boolean flip = true;
  static void f() {
    int a = 2;
    int b = 3;
    while(true) {
      try {
        //if(flip)
        break;
      } finally {
          a = b; 
      }
    }
  }     
}


public class Inner11 {
    public void bar() {
        class BBB {
            class DDD {
                public void bar3() {
                    System.out.println(BBB.this);
                }
            }
        }
    }
}



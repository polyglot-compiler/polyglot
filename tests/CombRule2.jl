// Test based on JLS Example 6.5.7.1-1.
// A method inherited from a superclass should hide methods 
// with the same name in an enclosing class.
// The rule is described in Section 15.12.1 of the JLS
// http://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.12.1
class CombRule2Super {
    void f2(String s)       {}
    void f3(String s)       {}
    void f3(int i1, int i2) {}
}

class CombRule2 {
    void f1(int i) {}
    void f2(int i) {}
    void f3(int i) {}

    void m() {
        new CombRule2Super() {
            {
                f2(0);  // compile-time error
            }
        };
    }
}


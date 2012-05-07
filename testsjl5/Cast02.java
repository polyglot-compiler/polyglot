import java.util.*;

class C {
    
    static Object m(Object x) { return x; }
    
    Object n(Object x) { return x; }
    
    D foo() { return new D(); }
    
    void test() {
        String s = (String) C.m("Hello");
        s = (String) this.n("Hello");
        D d = new D();
        s = d.bar();
        s = ((D) this.foo()).bar();
        s = d.what;
        Integer i = (Integer) d.foo;
        i = (Integer) d.m();
    }
    
    public C() { super(); }
}

class D {
    Object foo;
    String what = "what?";
    
    Object m() { return foo; }
    
    String bar() { return "hi"; }
    
    public D() { super(); }
}

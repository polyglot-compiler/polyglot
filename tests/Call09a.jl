interface T151222a9b {
    void m(String s, Object o);
}
abstract class T151222a9c extends p1.T151222a9a implements T151222a9b {}
abstract class T151222a9d extends T151222a9c {
    // Even though d inherits two versions of m, the protected a.m
    // is only accessible if the qualifying expression is type d or lower
    void foo(T151222a9d d) {
        d.m("", "");
    } // both a.m(Object, String) and b.m(String, Object) are accessible
}

package p1;
public class T8462h4a {
    void m() {}
}
class T8462h4c extends p2.T8462h4b {
    // if this were not static, it would override a.m. Therefore, there is
    // a conflict, and this static method is hiding an instance method, even
    // though a.m is not inherited
    static void m() {}
}

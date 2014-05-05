package p1;
public class T6541s8a {
    class p1 {}
}
class T6541s8c extends p2.T6541s8b {
    // class p1 was not inherited, although it is accessible
    // p1 resolves to a package
    p1 p;
}

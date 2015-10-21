import package1.MemberClass02a;

class C extends MemberClass02a {
    void foo() {
        MemberClass02a x = null;
        C.Inner y = x.new Inner();
    }
    class Inner extends MemberClass02a.Inner {}
}

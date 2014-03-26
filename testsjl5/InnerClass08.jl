class TreeMap { }
class NavSubmap {
    class SubmapKi { 
        SubmapKi(TreeMap o) {
        }
        void bar(TreeMap o) {
        }
    }
}
class AscSubmap extends NavSubmap {
    public void foo(AscSubmap.SubmapKi c, TreeMap a) {
        // both of these calls appear 
        // to fail for the same reason.
        AscSubmap.SubmapKi b = this.new SubmapKi(a);
        c.bar(a);
    }
}

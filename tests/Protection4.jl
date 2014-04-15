class N {
    private static class O extends M {
        public void func() {
            eq(); // should resolve to N.eq()
        }
    }
    static void eq() {}
}

class M {
    private static void eq() {} // inaccessible from N.O.
}

public class LocalInners2 {
    public static void main(final String[] args) {
        class Local {
            class XInner0 extends Local {
            }
            class XInner1 extends Local {
                int arglen = args.length;
            }
        }
        new Local().new XInner0();
        new Local().new XInner1();
        foo(new Local().new XInner1().arglen);
    }

    static void foo(int i) {}
}

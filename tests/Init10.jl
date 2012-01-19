class C {
    private static final C securitySupport;
    static {
        C ss = null;
        try {
            Class c = Class.forName("java.security.AccessController");
            ss = new C();
        } catch (Exception ex) {
            // ignore it
        } finally { 
            if (ss == null)
                ss = new C();
            securitySupport = ss;
        }
    }
}

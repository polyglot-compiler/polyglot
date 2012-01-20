class C {
    private static final C securitySupport;
    static {
        C ss = null;	
	try {
            ss = new C();
        } finally { 
            if (ss == null)
                ss = new C();
            securitySupport = ss;
	}
    }
}

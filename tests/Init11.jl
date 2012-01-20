class C {
    C securitySupport;
    static {
	C ss = null;
        try {
	    ss = new C();
        } catch (Exception ex) {
            // ignore it
        } finally { 
             if (ss == null)
                 ss = new C();
	     
	}
    }
}

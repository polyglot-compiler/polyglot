class C {
    final static int f;
    static {
        try {
	    if (234 <54) {
		throw new ClassNotFoundException();
	    }
        } catch (ClassNotFoundException e) {
        } finally {
            f = 42;

        }
    }


}

class T414dut1 {
    T414dut1 (){}
    public static void main(String[] args) {
        
        final boolean b;
        try {
            assert b = false;
        } catch (AssertionError e) {
            b = false;
        }
    
    }
}

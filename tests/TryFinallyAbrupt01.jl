class T1629dap2 {
    T1629dap2 (){}
    public static void main(String[] args) {

        int i;
        while (true)
            try {
                break; // not an exiting break;
            } finally {
                i = 1;
                break;
            }
        int j = i;

    }
}

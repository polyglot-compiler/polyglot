class T16214duf2 {
    T16214duf2 (){}
    public static void main(String[] args) {

        final int i;
        int count = 0;
        while (true) {
            try {
                i = count++;
                count /= 0; // throws ArithmeticException
                while (true);
            } finally {
                // i not DU, since try block had reachable assignment
                continue; // discards exception
            }
            // i multiply assigned in loop
        }

    }
}

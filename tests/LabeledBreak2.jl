class LabeledBreak {
    public static void main(String[] args) {
        int i;
        a: for (i=0; i<10; ++i) {
            break a;
        }
        System.out.println("i = " + i); // prints "i = 0"
    }
}

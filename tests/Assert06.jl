class T414l13 {
    T414l13 (){}
    public static void main(String[] args) {

        final boolean b;
        while (true)
            assert false : b = true; // if assigned, loop completes abruptly

    }
}

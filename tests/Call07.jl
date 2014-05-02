class ArrayIsObject {
    public static void main(String[] args) {
        int len = args.length;
        System.out.println(len);
        Class c = args.getClass();
        System.out.println(c);
        String s = args.toString();
        System.out.println(s);
    }
}

public class UniqueID {
    private static int count = 0;

    public static String UniqueID(String s) {
	String uid = s + "$" + count;
	count++;
	return uid;
    }
}

public class UniqueID {
    private static int count = 0;
    private String uid;

    public UniqueID(String s) {
	uid = s + "$" + count;
	count++;
    }

    public boolean equals(Object o) {
	if (! (o instanceof UniqueID)) {
	    return false;
	} else {
	    return this.uid.equals(((UniqueID)o).uid);
	}
    }
}

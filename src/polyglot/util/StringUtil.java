package jltools.util;

/** String utilities. */
public class StringUtil
{
    /**
     * Given the name for a class, returns the portion which appears to
     * constitute the package -- i.e., all characters up to but not including
     * the last dot, or no characters if the name has no dot.
     **/
    public static String getPackageComponent(String fullName) {
	int lastDot = fullName.lastIndexOf('.');
	return lastDot >= 0 ? fullName.substring(0,lastDot) : "";
    }
   
    /**
     * Given the name for a class, returns the portion which appears to
     * constitute the package -- i.e., all characters after the last
     * dot, or all the characters if the name has no dot.
     **/
    public static String getShortNameComponent(String fullName) {
	int lastDot = fullName.lastIndexOf('.');
	return lastDot >= 0 ? fullName.substring(lastDot+1) : fullName;
    }

    /**
     * Returns true iff the provided class name does not appear to be
     * qualified (i.e., it has no dot.)
     **/
    public static boolean isNameShort(String name) {
	return name.indexOf('.') < 0;
    }

    public static String getFirstComponent(String fullName) {
	int firstDot = fullName.indexOf('.');
	return firstDot >= 0 ? fullName.substring(0,firstDot) : fullName;
    }

    public static String removeFirstComponent(String fullName) {
	int firstDot = fullName.indexOf('.');
	return firstDot >= 0 ? fullName.substring(firstDot+1) : "";
    }
 
    public static String escape(String s) {
        StringBuffer sb = new StringBuffer(s.length());

	for (int i = 0; i < s.length(); i++) {
	    char c = s.charAt(i);
	    sb.append(escape(c));
	}

	return sb.toString();
    }

    public static String escape(char c) {
        if (c > 0xff) {
	    return "" + c;
	}

	switch (c) {
	    case '\b': return "\\b";
	    case '\t': return "\\t";
	    case '\n': return "\\n";
	    case '\f': return "\\f";
	    case '\r': return "\\r";
	    case '\"': return "\\" + c; // "\\\"";
	    case '\'': return "\\" + c; // "\\\'";
	    case '\\': return "\\" + c; // "\\\\";
	}

	if (c >= 0x20 && c < 0x7f) {
	    return "" + c;
	}

	return "\\" + (char) ('0' + c / 64)
		    + (char) ('0' + (c & 63) / 8)
		    + (char) ('0' + (c & 7));
    }
}

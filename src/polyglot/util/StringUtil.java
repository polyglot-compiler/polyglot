/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.util;

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
        return escape(s, false);
    }

    public static String escape(char c) {
        String t = escape(c, false);
        if (t != null) return t;
        return String.valueOf(c);
    }

    public static String unicodeEscape(String s) {
        return escape(s, true);
    }

    public static String unicodeEscape(char c) {
        String t = escape(c, true);
        if (t != null) return t;
        return String.valueOf(c);
    }

    public static String escape(String s, boolean unicode) {
        StringBuffer sb = null;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            String t = escape(c, unicode);
            if (sb == null && t != null) {
                // string to return will not be the same
                // as the original.
                sb = new StringBuffer(s.length() + 10);
                sb.append(s.substring(0, i));
            }
            
            if (sb != null) {
                if (t != null) {
                    sb.append(t);
                }
                else {
                    sb.append(c);
                }
            }
        }

        if (sb != null) return sb.toString();
        return s;
    }

    /**
     * Returns the string that c escapes to, null if
     * c does not require escaping.
     */
    private static String escape(char c, boolean unicode) {
        if (c > 0xff) {
            if (unicode) {
                StringBuffer sb = new StringBuffer(8);
                sb.append(Integer.toHexString(c));
                while (sb.length() < 4) sb.insert(0, '0');
                sb.insert(0, "\\u");
                return sb.toString();
            }
            else {
                return null;
            }
        }

        switch (c) {
        case '\b': return ("\\b");
        case '\t': return ("\\t");
        case '\n': return ("\\n");
        case '\f': return ("\\f");
        case '\r': return ("\\r");
        case '\"': return ("\\\""); // "\\\"";
        case '\'': return ("\\\'"); // "\\\'";
        case '\\': return ("\\\\"); // "\\\\";
        }

        if (c >= 0x20 && c < 0x7f) {
            return null;
        }

        return ("\\" + (char) ('0' + c / 64)
                  + (char) ('0' + (c & 63) / 8)
                  + (char) ('0' + (c & 7)));
    }

    public static String nth(int n) {
        StringBuffer s = new StringBuffer(String.valueOf(n));
        if (s.length() > 1) {
            if (s.charAt(s.length()-2) == '1') {
                // all the teens end in "th", e.g. "11th"
                s.append("th");
                return s.toString();                
            }            
        }

        char last = s.charAt(s.length()-1);
        switch (last) {
        case '1':
            s.append("st");
            break;
        case '2':
            s.append("nd");
            break;
        case '3':
            s.append("rd");
            break;
        default:
            s.append("th");

        }
        return s.toString();
    }
}

package polyglot.types;

import java.util.*;
import polyglot.main.Report;

/** A class used for reporting debug output related to types. */
public class Types
{
    /**
     * Print debug messages with topic "types".
     */
    public static boolean should_report(int level) {
        return Report.should_report("types", level);
    }
    public static void report(int level, String msg) {
        Report.report(level, msg);
    }
}

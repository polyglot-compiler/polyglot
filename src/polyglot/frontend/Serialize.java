package polyglot.frontend;

import java.util.*;
import polyglot.main.Report;

/** A class used for reporting debug output related to serialization. */
public class Serialize
{
    /**
     * Decide whether to print debug messages with topic "serialize".
     */
    public static boolean should_report(int level) {
        return Report.should_report("serialize", level);
    }

    /**
     * Print debug messages with topic "serialize".
     */
    public static void report(int level, String msg) {
        Report.report(level, msg);
    }
}

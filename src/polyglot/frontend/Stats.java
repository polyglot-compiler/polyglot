package polyglot.frontend;

import java.util.*;
import java.io.PrintStream;

import polyglot.util.*;
import polyglot.main.Report;

/**
 * Statistics collection and reporting object.
 * Extensions can override this to collect more stats or to change
 * reporting.
 */
public class Stats
{
    protected static class Times {
        long inclusive;
        long exclusive;
    }

    /** Map from Pass.ID to pair of inclusive and exclusive times. */
    protected Map passTimes = new HashMap();

    /** Reset the accumulated times for a pass. */
    public void resetPassTimes(Pass.ID id) {
        passTimes.remove(id);
    }

    /** Return the accumulated times for a pass. */
    public long passTime(Pass.ID id, boolean inclusive) {
        Times t = (Times) passTimes.get(id);
        if (t == null) {
            return 0;
        }

        return inclusive ? t.inclusive : t.exclusive;
    }

    /** Accumulate inclusive and exclusive times for a pass. */
    public void accumPassTimes(Pass.ID id, long in, long ex) {
        Times t = (Times) passTimes.get(id);
        if (t == null) {
            t = new Times();
            passTimes.put(id, t);
        }
        t.inclusive += in;
        t.exclusive += ex;
    }

    /** Report the stats. */
    public void report() {
        if (Report.should_report(Report.time, 1)) {
            Report.report(1, "Pass Inclusive Exclusive");
            Report.report(1, "---- --------- ---------");

            for (Iterator i = passTimes.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry) i.next();
                Pass.ID id = (Pass.ID) e.getKey();
                Times t = (Times) e.getValue();

                Report.report(1, id.toString() + " " +
                                 t.inclusive + " " + t.exclusive);
            }
        }
    }
}

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

    /** Extension we're collecting stats for. */
    protected ExtensionInfo ext;

    /** Map from Pass.ID to pair of inclusive and exclusive times. */
    protected Map passTimes = new HashMap();

    /**
     * List of Pass.IDs used as keys to passTimes.  We have an explicit
     * list in order to report the passes in order.
     */
    protected List passes = new ArrayList(20);

    public Stats(ExtensionInfo ext) {
        this.ext = ext;
    }

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
            passes.add(id);
            t = new Times();
            passTimes.put(id, t);
        }
        t.inclusive += in;
        t.exclusive += ex;
    }

    /** Report the stats. */
    public void report() {
        if (Report.should_report(Report.time, 1)) {
            Report.report(1, "\nStatistics for " + ext.compilerName() +
                          " (" + ext.getClass().getName() + ")");
            Report.report(1, "Pass Inclusive Exclusive");
            Report.report(1, "---- --------- ---------");

            for (Iterator i = passes.iterator(); i.hasNext(); ) {
                Pass.ID id = (Pass.ID) i.next();
                Times t = (Times) passTimes.get(id);

                Report.report(1, id.toString() + " " +
                                 t.inclusive + " " + t.exclusive);
            }
        }
    }
}

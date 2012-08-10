/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.frontend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import polyglot.main.Report;

/**
 * Statistics collection and reporting object.
 * Extensions can override this to collect more stats or to change
 * reporting.
 */
public class Stats {
    protected static class Times {
        public long inclusive;
        public long exclusive;
    }

    /** Extension we're collecting stats for. */
    protected ExtensionInfo ext;

    /** Map from Objects to pair of inclusive and exclusive times. */
    protected Map<Object, Times> passTimes = new HashMap<Object, Times>();

    /**
     * List of Objects used as keys to passTimes.  We have an explicit
     * list in order to report the keys in order.
     */
    protected List<Object> keys = new ArrayList<Object>(20);

    public Stats(ExtensionInfo ext) {
        this.ext = ext;
    }

    /** Reset the accumulated times for a pass. */
    public void resetPassTimes(Object key) {
        passTimes.remove(key);
    }

    /** Return the accumulated times for a pass. */
    public long passTime(Object key, boolean inclusive) {
        Times t = passTimes.get(key);
        if (t == null) {
            return 0;
        }

        return inclusive ? t.inclusive : t.exclusive;
    }

    /** Accumulate inclusive and exclusive times for a pass. */
    public void accumPassTimes(Object key, long in, long ex) {
        Times t = passTimes.get(key);
        if (t == null) {
            keys.add(key);
            t = new Times();
            passTimes.put(key, t);
        }
        t.inclusive += in;
        t.exclusive += ex;
    }

    /** Report the stats. */
    public void report() {
        if (Report.should_report(Report.time, 1)) {
            Report.report(1, "\nStatistics for " + ext.compilerName() + " ("
                    + ext.getClass().getName() + ")");
            Report.report(1, "Inclusive Exclusive Key");
            Report.report(1, "--------- --------- ---");

            for (Object key : keys) {
                Times t = passTimes.get(key);

                Report.report(1,
                              t.inclusive + " " + t.exclusive + " "
                                      + key.toString());
            }
        }
    }
}

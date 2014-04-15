/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package polyglot.pth;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import polyglot.util.ErrorQueue;

/**
 * 
 */
public abstract class OutputController {
    protected final PrintStream out;
    protected final Calendar today;
    protected final Calendar week;

    public OutputController(PrintStream out) {
        this.out = out;
        today = Calendar.getInstance();
        week = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        today.clear();
        today.set(now.get(Calendar.YEAR),
                  now.get(Calendar.MONTH),
                  now.get(Calendar.DATE));
        week.setTimeInMillis(today.getTimeInMillis());
        week.add(Calendar.DATE, -6);
    }

    public void startTest(Test t) {
        if (t instanceof ScriptTestSuite) {
            startScriptTestSuite((ScriptTestSuite) t);
        }
        else if (t instanceof SourceFileTest) {
            startSourceFileTest((SourceFileTest) t);
        }
    }

    public void finishTest(Test t, ErrorQueue eq) {
        if (t instanceof ScriptTestSuite) {
            finishScriptTestSuite((ScriptTestSuite) t);
        }
        else if (t instanceof SourceFileTest) {
            finishSourceFileTest((SourceFileTest) t, eq);
        }
    }

    protected abstract void startScriptTestSuite(ScriptTestSuite sts);

    protected abstract void startSourceFileTest(SourceFileTest sft);

    protected abstract void finishScriptTestSuite(ScriptTestSuite sts);

    protected abstract void finishSourceFileTest(SourceFileTest sft,
            ErrorQueue eq);

    public abstract void displayTestSuiteResults(String suiteName, TestSuite ts);

    public abstract void displayTestResults(TestResult tr, String testName);

    protected DateFormat getDefaultDateFormat() {
        return new SimpleDateFormat("d-MMM-yy");
    }

    protected DateFormat getSameYearDateFormat() {
        return new SimpleDateFormat("d-MMM");
    }

    protected DateFormat getSameWeekDateFormat() {
        return new SimpleDateFormat("EEE k:mm");
    }

    protected DateFormat getTodayDateFormat() {
        return new SimpleDateFormat("k:mm");
    }

    public String getDateDisplay(Date d) {
        if (d == null) return "never";

        DateFormat df;
        Calendar dt = Calendar.getInstance();
        dt.setTime(d);

        if (dt.after(today)) {
            df = getTodayDateFormat();
        }
        else if (dt.after(week)) {
            df = getSameWeekDateFormat();
        }
        else if (dt.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            df = getSameYearDateFormat();
        }
        else {
            df = getDefaultDateFormat();
        }

        return df.format(d);
    }

    public abstract void warning(String w);
}

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

package polyglot.main;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Stack;

import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.Position;
import polyglot.util.SimpleErrorQueue;

/** Class used for reporting debug messages. */
public class Report {
    /** A collection of string names of topics which can be used with the
        -report command-line switch */
    public final static Collection<String> topics = new LinkedHashSet<>();

    /** A collection of string names of topics which we should always check
        if we should report. */
    public final static Stack<String> should_report = new Stack<>();

    /** 
     * The topics that the user has selected to report, mapped to the level
     * they want to report them to.
     */
    protected final static Map<String, Integer> reportTopics = new HashMap<>();

    /** Error queue to which to write messages. */
    protected static ErrorQueue eq;

    /**
     * Indicates if there is no reporting at all.
     * The normal case is that we do not report anything, so for efficiency 
     * reasons, since {@code should_report} is called so often, we'll use
     * this flag to bypass a lot of the checking. When the options are processed,
     * this flag should be changed.
     */
    protected static boolean noReporting = true;

    /** Report topics understood by the base compiler. */
    public final static String cfg = "cfg";
    public final static String context = "context";
    public final static String dataflow = "dataflow";
    public final static String errors = "errors";
    public final static String frontend = "frontend";
    public final static String imports = "imports";
    public final static String loader = "loader";
    public final static String resolver = "resolver";
    public final static String serialize = "serialize";
    public final static String time = "time";
    public final static String types = "types";
    public final static String visit = "visit";
    public final static String verbose = "verbose";

    // This topic is the level of detail that should be in messages.
    public final static String debug = "debug";

    static {
        topics.add(cfg);
        topics.add(context);
        topics.add(dataflow);
        topics.add(errors);
        topics.add(frontend);
        topics.add(imports);
        topics.add(loader);
        topics.add(resolver);
        topics.add(serialize);
        topics.add(time);
        topics.add(types);
        topics.add(visit);
        topics.add(verbose);
        topics.add(debug);

        pushTopic(verbose);
    }

    /**
     * Return whether a message on {@code topic} of obscurity
     * {@code level} should be reported, based on use of the
     * -report command-line switches given by the user.
     */
    public static boolean should_report(String topic, int level) {
        if (noReporting) return false;
        return should_report(Collections.singletonList(topic), level);
    }

    /**
     * Return whether a message on {@code topics} of obscurity
     * {@code level} should be reported, based on use of the
     * -report command-line switches given by the user.
     */
    public static boolean should_report(String[] topics, int level) {
        if (noReporting) return false;
        synchronized (should_report) {
            for (String topic : should_report) {
                if (level(topic) >= level) return true;
            }
        }
        if (topics != null) {
            for (String topic : topics) {
                if (level(topic) >= level) return true;
            }
        }
        return false;
    }

    public static void pushTopic(String topic) {
        should_report.push(topic);
    }

    public static void popTopic() {
        if (should_report.isEmpty()) return;
        should_report.pop();
    }

    /**
     * Return whether a message on {@code topics} of obscurity
     * {@code level} should be reported, based on use of the
     * -report command-line switches given by the user.
     */
    public static boolean should_report(Collection<String> topics, int level) {
        if (noReporting) return false;
        synchronized (should_report) {
            for (String topic : should_report) {
                if (level(topic) >= level) return true;
            }
        }
        if (topics != null) {
            for (String topic : topics) {
                if (level(topic) >= level) return true;
            }
        }
        return false;
    }

    public static void addTopic(String topic, int level) {
        Integer i = reportTopics.get(topic);
        if (i == null || i.intValue() < level) {
            reportTopics.put(topic, new Integer(level));
        }
        noReporting = false;
    }

    public static void setTopic(String topic, int level) {
        if (reportTopics.containsKey(topic)) reportTopics.put(topic, level);
    }

    /** Get the error queue, possibly creating it if not set. */
    public static ErrorQueue getQueue() {
        if (eq == null) {
            eq = new SimpleErrorQueue();
        }
        return eq;
    }

    /** Set the error queue. */
    public static void setQueue(ErrorQueue eq) {
        Report.eq = eq;
    }

    public static int level(String name) {
        Object i = reportTopics.get(name);
        if (i == null)
            return 0;
        else return ((Integer) i).intValue();
    }

    /** This is the standard way to report debugging information in the
     *  compiler.  It reports a message of the specified level (which
     *  controls the presentation of the message. To test whether such
     *  message should be reported, use "should_report".
     *
     *  NOTE: This is a change of spec from earlier versions of Report.
     *  NOTE: If position information is available, call report(int, String, Position)
     *  instead, to ensure the error is associated with the right file/location.
     */
    public static void report(int level, String message) {
        report(level, message, null);
    }

    /** This is the standard way to report debugging information in the
     *  compiler.  It reports a message of the specified level (which
     *  controls the presentation of the message. To test whether such
     *  message should be reported, use "should_report".
     *
     *  NOTE: This is a change of spec from earlier versions of Report.
     *  NOTE: This version takes an explicit Position, so that position info gets
     *  properly associated with the ErrorInfo that gets created by enqueue().
     */
    public static void report(int level, String message, Position pos) {
        StringBuffer buf = new StringBuffer(message.length() + level);
        for (int j = 1; j < level; j++) {
            buf.append(" ");
        }
        buf.append(message);
        getQueue().enqueue(ErrorInfo.DEBUG, buf.toString(), pos);
    }
}

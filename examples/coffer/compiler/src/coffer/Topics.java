/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer;

import polyglot.main.Report;

/**
 * Extension information for coffer extension.
 */
public class Topics {
    public static final String coffer = "coffer";
    public static final String keycheck = "keycheck";

    static {
        Report.topics.add(coffer);
        Report.topics.add(keycheck);
    }
}

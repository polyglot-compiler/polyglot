/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.frontend;

import polyglot.main.Report;

/**
 * Extension information for jl extension.
 */
public class Topics {
    public static final String jl = "jl";
    public static final String qq = "qq";

    static {
        Report.topics.add(jl);
        Report.topics.add(qq);
    }
}

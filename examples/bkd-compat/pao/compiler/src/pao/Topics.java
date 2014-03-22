/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao;

import polyglot.main.Report;

/**
 * Additional report topics for the PAO extension.
 */
public class Topics {
    public static final String pao = "pao";

    static {
        // add the additional report topics to the Report class.
        Report.topics.add(pao);
    }
}

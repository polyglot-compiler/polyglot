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

import java.io.File;
import java.util.Date;
import java.util.Map;

import polyglot.util.SerialVersionUID;

/**
 *
 */
public final class TestSuiteResult extends TestResult {
    private static final long serialVersionUID = SerialVersionUID.generate();
    private static final String RESULT_FILE_SUFFIX = ".results";
    private static final String RESULT_FILE_PREFIX = "";

    /**
     * The name of the results file is the name of script file,
     * bracketed with RESULTS_FILE_PREFIX and RESULT_FILE_SUFFIX.
     */
    protected static String getResultsFileName(File script) {
        String parent = "";
        if (script.getParent() != null)
            parent = script.getParent() + File.separator;
        return parent + RESULT_FILE_PREFIX + script.getName()
                + RESULT_FILE_SUFFIX;
    }

    public final Map<String, TestResult> testResults;

    public TestSuiteResult(Test t, Date dateTestRun,
            Map<String, TestResult> testResults, Date dateLastSuccess) {
        super(t, dateTestRun, dateLastSuccess);
        this.testResults = testResults;
    }
}

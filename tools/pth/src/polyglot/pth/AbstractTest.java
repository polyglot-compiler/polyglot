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

import java.util.Date;

/**
 * 
 */
public abstract class AbstractTest implements Test {
    protected String name;
    protected String description;
    protected boolean success = false;
    protected boolean hasRun = false;
    protected String failureMessage = null;
    protected TestResult testResult;

    protected OutputController output;

    public AbstractTest(String name) {
        this.name = name;
    }

    @Override
    public void setOutputController(OutputController output) {
        this.output = output;
    }

    @Override
    public final boolean run() {
        preRun();
        this.success = this.runTest();

        this.hasRun = true;
        Date lastSuccess = null;
        if (this.getTestResult() != null) {
            lastSuccess = this.getTestResult().dateLastSuccess;
        }
        this.setTestResult(this.createTestResult(lastSuccess));
        postRun();
        return success();
    }

    protected abstract boolean runTest();

    protected void preRun() {
        output.startTest(this);
    }

    protected void postRun() {
        output.finishTest(this, null);
    }

    protected TestResult createTestResult(Date lastSuccess) {
        Date lastRun = new Date();
        if (this.success()) {
            lastSuccess = lastRun;
        }
        return new TestResult(this, lastRun, lastSuccess);
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setDescription(String string) {
        description = string;
    }

    public void setName(String string) {
        name = string;
    }

    @Override
    public TestResult getTestResult() {
        return testResult;
    }

    @Override
    public void setTestResult(TestResult tr) {
        testResult = tr;
    }
}

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

/**
 *
 */
public class QuietOutputController extends StdOutputController {
    private final boolean showStartScript;
    private final boolean showFinishScript;
    private final boolean showScriptProgress;
    private final boolean showStartFile;
    private final boolean showFinishFile;
    private int count = 0;

    private static final char PROGRESS_PASS_CHAR = '.';
    private static final char PROGRESS_FAIL_CHAR = '!';
    private static final int PROGRESS_LIMIT = 60;

    public QuietOutputController(PrintStream out, boolean showStartScript,
            boolean showFinishScript, boolean showStartFile,
            boolean showFinishFile, boolean showScriptProgress) {
        super(out);
        this.showStartScript = showStartScript;
        this.showFinishScript = showFinishScript;
        this.showScriptProgress = showScriptProgress;
        this.showStartFile = showStartFile;
        this.showFinishFile = showFinishFile;
    }

    @Override
    protected void startScriptTestSuite(ScriptTestSuite sts) {
        if (showStartScript)
            super.startScriptTestSuite(sts);
        else beginBlock();
    }

    @Override
    protected void finishScriptTestSuite(ScriptTestSuite sts) {
        if (showScriptProgress && !printIndent) println();
        if (showFinishScript)
            super.finishScriptTestSuite(sts);
        else endBlock();
    }

    @Override
    protected void startSourceFileTestCollection(
            SourceFileTestCollection sftc) {
        if (showStartScript)
            super.startSourceFileTestCollection(sftc);
        else beginBlock();
    }

    @Override
    protected void finishSourceFileTestCollection(
            SourceFileTestCollection sftc) {
        if (showScriptProgress && !printIndent) println();
        if (showFinishScript)
            super.finishSourceFileTestCollection(sftc);
        else endBlock();
    }

    @Override
    protected void startSourceFileTest(SourceFileTest sft) {
        if (showStartFile)
            super.startSourceFileTest(sft);
        else beginBlock();
    }

    @Override
    protected void finishSourceFileTest(SourceFileTest sft) {
        if (showFinishFile)
            super.finishSourceFileTest(sft);
        else {
            endBlock();
            if (showScriptProgress) {
                print(sft.success() ? PROGRESS_PASS_CHAR : PROGRESS_FAIL_CHAR);
                if (++count >= PROGRESS_LIMIT - indent) {
                    println();
                    count = 0;
                }
            }
        }
    }

    @Override
    protected void startBuildTest(BuildTest b) {
        if (showStartFile)
            super.startBuildTest(b);
        else beginBlock();
    }

    @Override
    protected void finishBuildTest(BuildTest b) {
        if (showFinishFile)
            super.finishBuildTest(b);
        else endBlock();
    }
}

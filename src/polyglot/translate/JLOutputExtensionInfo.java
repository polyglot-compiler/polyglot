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
package polyglot.translate;

import java.util.Collections;
import java.util.List;

import polyglot.frontend.EmptyPass;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.JLExtensionInfo;
import polyglot.frontend.JLScheduler;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.SourceFileGoal;
import polyglot.main.OptFlag.Arg;
import polyglot.main.Options;
import polyglot.main.UsageError;
import polyglot.util.InternalCompilerError;

/**
 * A simple example of the output extension pattern.  This extension 
 * uses a parent extension to set its options and expects all jobs 
 * to be enqueued directly in its scheduler by an ExtensionRewriter.
 * 
 * It also provides a subclass of JLScheduler that does performs an 
 * empty pass for the Parsed goal, since no files are actually parsed.
 */
public class JLOutputExtensionInfo extends JLExtensionInfo {
    final protected ExtensionInfo parent;

    public JLOutputExtensionInfo(ExtensionInfo parent) {
        this.parent = parent;
    }

    @Override
    public Scheduler createScheduler() {
        return new JLOutputScheduler(this);
    }

    static protected class JLOutputOptions extends Options {

        public JLOutputOptions(ExtensionInfo extension) {
            super(extension);
        }

        /**
         * Skip checks regarding source files.
         */
        @Override
        protected void validateArgs() throws UsageError {
        }

    }

    @Override
    protected Options createOptions() {
        Options parentOpts = parent.getOptions();
        Options opt = new JLOutputOptions(this);
        // filter the parent's options by the ones this extension understands
        List<Arg<?>> arguments = parentOpts.filterArgs(opt.flags());
        try {
            opt.processArguments(arguments, Collections.<String> emptySet());
        }
        catch (UsageError e) {
            throw new InternalCompilerError("Got usage error while configuring output extension",
                                            e);
        }
        return opt;
    }

    static protected class JLOutputScheduler extends JLScheduler {
        public JLOutputScheduler(ExtensionInfo extInfo) {
            super(extInfo);
        }

        @Override
        public Goal Parsed(Job job) {
            return internGoal(new SourceFileGoal(job) {
                @Override
                public Pass createPass(ExtensionInfo extInfo) {
                    return new EmptyPass(this);
                }
            });
        }
    }
}

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

package polyglot.frontend.goals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import polyglot.ast.NodeFactory;
import polyglot.frontend.Compiler;
import polyglot.frontend.EmptyPass;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.Scheduler;
import polyglot.frontend.VisitorPass;
import polyglot.main.Version;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import polyglot.visit.ClassSerializer;

/**
 * The {@code Serialized} goal is reached after typing information is serialized
 * into the compiled code.
 */
public class Serialized extends SourceFileGoal {
    public static Goal create(Scheduler scheduler, Job job) {
        return scheduler.internGoal(new Serialized(job));
    }

    protected Serialized(Job job) {
        super(job);
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        if (extInfo.getOptions().serialize_type_info) {
            Compiler compiler = extInfo.compiler();
            TypeSystem ts = extInfo.typeSystem();
            NodeFactory nf = extInfo.nodeFactory();
            return new VisitorPass(
                    this,
                    createSerializer(
                            ts,
                            nf,
                            job().source().getLastModified(),
                            compiler.errorQueue(),
                            extInfo.version()));
        } else {
            return new EmptyPass(this);
        }
    }

    protected ClassSerializer createSerializer(
            TypeSystem ts, NodeFactory nf, long lastModified, ErrorQueue eq, Version version) {
        return new ClassSerializer(ts, nf, lastModified, eq, version);
    }

    @Override
    public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
        List<Goal> l = new ArrayList<>();
        l.add(scheduler.Validated(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }
}

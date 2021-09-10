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

package polyglot.visit;

import java.util.Map;

import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.TypeSystem;
import polyglot.types.VarInstance;

public class DefiniteAssignmentChecker
        extends AbstractDefiniteAssignmentChecker<
                DefiniteAssignmentChecker.ClassBodyInfo, AbstractAssignmentChecker.FlowItem> {
    public DefiniteAssignmentChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    protected static class ClassBodyInfo
            extends AbstractAssignmentChecker.ClassBodyInfo<ClassBodyInfo> {
        public ClassBodyInfo(ClassBodyInfo outer, ClassType curClass) {
            super(outer, curClass);
        }
    }

    @Override
    protected FlowItem newFlowItem(
            Map<VarInstance, AssignmentStatus> map, boolean canTerminateNormally) {
        return new FlowItem(map, canTerminateNormally);
    }

    @Override
    protected ClassBodyInfo newCBI(ClassBodyInfo prevCBI, ClassType ct) {
        return new ClassBodyInfo(curCBI, ct);
    }
}

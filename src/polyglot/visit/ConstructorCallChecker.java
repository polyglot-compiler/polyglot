/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.visit;

import java.util.HashMap;
import java.util.Map;

import polyglot.ast.ConstructorCall;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;

/** Visitor which ensures that constructor calls are not recursive. */
public class ConstructorCallChecker extends ContextVisitor {
    public ConstructorCallChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    protected Map<ConstructorInstance, ConstructorInstance> constructorInvocations =
            new HashMap<ConstructorInstance, ConstructorInstance>();

    @Override
    protected NodeVisitor enterCall(Node n) throws SemanticException {
        if (n instanceof ConstructorCall) {
            ConstructorCall cc = (ConstructorCall) n;
            if (cc.kind() == ConstructorCall.THIS) {
                // the constructor calls another constructor in the same class
                Context ctxt = context();

                if (!(ctxt.currentCode() instanceof ConstructorInstance)) {
                    throw new InternalCompilerError("Constructor call "
                            + "occurring in a non-constructor.", cc.position());
                }
                ConstructorInstance srcCI =
                        ((ConstructorInstance) ctxt.currentCode()).orig();
                ConstructorInstance destCI = cc.constructorInstance().orig();

                constructorInvocations.put(srcCI, destCI);
                while (destCI != null) {
                    destCI = constructorInvocations.get(destCI);
                    if (destCI != null && srcCI.equals(destCI)) {
                        // loop in the constructor invocations!
                        throw new SemanticException("Recursive constructor "
                                + "invocation.", cc.position());
                    }
                }
            }
        }
        return this;
    }
}

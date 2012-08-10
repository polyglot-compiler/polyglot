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

import java.util.HashSet;
import java.util.Set;

import polyglot.ast.Assign;
import polyglot.ast.Formal;
import polyglot.ast.Local;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.SourceFile;
import polyglot.ast.Unary;
import polyglot.frontend.Job;
import polyglot.types.LocalInstance;
import polyglot.types.TypeSystem;

/**
 * This visitor converts non-final local variables into final local variables.
 * This improves the precision of some analyses.
 *
 * @author nystrom
 */
public class FinalLocalExtractor extends NodeVisitor {

    /** Set of LocalInstances declared final; these should not be made non-final. */
    protected Set<LocalInstance> isFinal;

    /**
     * @param job
     * @param ts
     * @param nf
     */
    public FinalLocalExtractor(Job job, TypeSystem ts, NodeFactory nf) {
        super();
    }

    @Override
    public NodeVisitor begin() {
        isFinal = new HashSet<LocalInstance>();
        return super.begin();
    }

    @Override
    public void finish() {
        isFinal = null;
    }

    // TODO: handle locals that are not initialized when declared
    //
    // TODO: handle anonymous classes: this visitor assumes all LocalInstances
    // are set correctly, which is true after disambiguation, except for anonymous
    // classes.
    //
    // TODO: convert to pseudo-SSA form: generate a new local decl when a local
    // is assigned, rather than marking the original as final.  If a local
    // requires a phi-function, just mark it non-final rather than generating
    // the phi.
    @Override
    public NodeVisitor enter(Node parent, Node n) {
        if (n instanceof Formal) {
            Formal d = (Formal) n;
            LocalInstance li = d.localInstance();
            if (!li.flags().isFinal()) {
                li.setFlags(li.flags().Final());
            }
            else {
                isFinal.add(li);
            }
        }
        if (n instanceof LocalDecl) {
            LocalDecl d = (LocalDecl) n;
            LocalInstance li = d.localInstance();
            if (!li.flags().isFinal()) {
                li.setFlags(li.flags().Final());
            }
            else {
                isFinal.add(li);
            }
        }
        if (n instanceof Unary) {
            Unary u = (Unary) n;
            if (u.expr() instanceof Local) {
                Local l = (Local) u.expr();
                LocalInstance li = l.localInstance().orig();
                if (u.operator() == Unary.PRE_DEC
                        || u.operator() == Unary.POST_DEC
                        || u.operator() == Unary.PRE_INC
                        || u.operator() == Unary.POST_INC) {
                    if (!isFinal.contains(li.orig())) {
                        li.setFlags(li.flags().clearFinal());
                    }
                }
            }
        }
        if (n instanceof Assign) {
            Assign a = (Assign) n;
            if (a.left() instanceof Local) {
                LocalInstance li = ((Local) a.left()).localInstance().orig();
                if (!isFinal.contains(li)) {
                    li.setFlags(li.flags().clearFinal());
                }
            }
        }
        return super.enter(parent, n);
    }

    protected static class LocalDeclFixer extends NodeVisitor {
        @Override
        public Node leave(Node old, Node n, NodeVisitor v) {
            if (n instanceof Formal) {
                Formal d = (Formal) n;
                return d.flags(d.localInstance().flags());
            }
            if (n instanceof LocalDecl) {
                LocalDecl d = (LocalDecl) n;
                return d.flags(d.localInstance().flags());
            }
            return n;
        }
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        // Revisit everything to ensure the local decls' flags agree with
        // their local instance's.
        if (n instanceof SourceFile) {
            return n.visit(new LocalDeclFixer());
        }
        return n;
    }
}

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

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Stmt;
import polyglot.ast.TypeNode;
import polyglot.frontend.Job;
import polyglot.types.Context;
import polyglot.types.TypeSystem;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public class SupertypeDisambiguator extends Disambiguator {
    public SupertypeDisambiguator(DisambiguationDriver dd) {
        super(dd);
    }

    public SupertypeDisambiguator(Job job, TypeSystem ts, NodeFactory nf,
            Context c) {
        super(job, ts, nf, c);
    }

    @Override
    public Node override(Node parent, Node n) {
        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;
            Node old = cd;

            // Call enter to handle scoping. 
            SupertypeDisambiguator v =
                    (SupertypeDisambiguator) enter(parent, cd);

            // Now visit the supertypes only.
            cd = cd.superClass((TypeNode) cd.visitChild(cd.superClass(), v));
            if (v.hasErrors()) return cd;

            List<TypeNode> newInterfaces = new ArrayList<TypeNode>();
            for (TypeNode tn : cd.interfaces()) {
                newInterfaces.add((TypeNode) cd.visitChild(tn, v));
                if (v.hasErrors()) return cd;
            }
            cd = cd.interfaces(newInterfaces);

            // Force the supertypes of cd.type() to be updated.
            cd = (ClassDecl) leave(parent, old, cd, v);
            if (this.hasErrors()) return cd;

            // Now visit the class body.
            cd = cd.body((ClassBody) cd.visitChild(cd.body(), v));
            if (v.hasErrors()) return cd;

            // Finally, rebulid the node again.
            return leave(parent, old, cd, v);
        }

        // Skip ClassMembers that are not ClassDecls.  These will be
        // handled by the SignatureDisambiguator visitor.
        if (n instanceof ClassMember) {
            return n;
        }

        if (n instanceof Stmt || n instanceof Expr) {
            return n;
        }

        return super.override(parent, n);
    }
}

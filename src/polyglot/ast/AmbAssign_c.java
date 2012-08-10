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

package polyglot.ast;

import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.CFGBuilder;
import polyglot.visit.TypeChecker;

/**
 * A <code>AmbAssign</code> represents a Java assignment expression to
 * an as yet unknown expression.
 */
public class AmbAssign_c extends Assign_c implements AmbAssign {
    public AmbAssign_c(Position pos, Expr left, Operator op, Expr right) {
        super(pos, left, op, right);
    }

    @Override
    public Term firstChild() {
        if (operator() != Assign.ASSIGN) {
            return left();
        }

        return right();
    }

    @Override
    protected void acceptCFGAssign(CFGBuilder<?> v) {
        v.visitCFG(right(), this, EXIT);
    }

    @Override
    protected void acceptCFGOpAssign(CFGBuilder<?> v) {
        v.visitCFG(left(), right(), ENTRY);
        v.visitCFG(right(), this, EXIT);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        Assign n = (Assign) super.disambiguate(ar);

        if (n.left() instanceof Local) {
            return ar.nodeFactory().LocalAssign(n.position(),
                                                (Local) left(),
                                                operator(),
                                                right());
        }
        else if (n.left() instanceof Field) {
            return ar.nodeFactory().FieldAssign(n.position(),
                                                (Field) left(),
                                                operator(),
                                                right());
        }
        else if (n.left() instanceof ArrayAccess) {
            return ar.nodeFactory().ArrayAccessAssign(n.position(),
                                                      (ArrayAccess) left(),
                                                      operator(),
                                                      right());
        }

        // LHS is still ambiguous.  The pass should get rerun later.
        return this;
        // throw new SemanticException("Could not disambiguate left side of assignment!", n.position());
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // Didn't finish disambiguation; just return.
        return this;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.AmbAssign(this.position, this.left, this.op, this.right);
    }
}

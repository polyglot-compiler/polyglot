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

import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * The Java literal <code>null</code>.
 */
public class NullLit_c extends Lit_c implements NullLit {
    public NullLit_c(Position pos) {
        super(pos);
    }

    /** Type check the expression. */
    @Override
    public Node typeCheck(TypeChecker tc) {
        return type(tc.typeSystem().Null());
    }

    /** Get the value of the expression, as an object. */
    public Object objValue() {
        return null;
    }

    @Override
    public String toString() {
        return "null";
    }

    /** Write the expression to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("null");
    }

    @Override
    public Object constantValue() {
        return null;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.NullLit(this.position);
    }

}

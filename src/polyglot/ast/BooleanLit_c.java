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

package polyglot.ast;

import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A {@code BooleanLit} represents a boolean literal expression.
 */
public class BooleanLit_c extends Lit_c implements BooleanLit {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected boolean value;

//    @Deprecated
    public BooleanLit_c(Position pos, boolean value) {
        this(pos, value, null);
    }

    public BooleanLit_c(Position pos, boolean value, Ext ext) {
        super(pos, ext);
        this.value = value;
    }

    @Override
    public boolean value() {
        return this.value;
    }

    @Override
    public BooleanLit value(boolean value) {
        return value(this, value);
    }

    protected <N extends BooleanLit_c> N value(N n, boolean value) {
        if (n.value == value) return n;
        n = copyIfNeeded(n);
        n.value = value;
        return n;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return type(tc.typeSystem().Boolean());
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(String.valueOf(value));
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(value " + value + ")");
        w.end();
    }

    @Override
    public Object constantValue(Lang lang) {
        return Boolean.valueOf(value);
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.BooleanLit(this.position, this.value);
    }

}

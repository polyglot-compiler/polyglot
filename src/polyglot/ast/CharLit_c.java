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
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.StringUtil;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/** 
 * An <code>CharLit</code> represents a literal in java of
 * <code>char</code> type.
 */
public class CharLit_c extends NumLit_c implements CharLit {
    public CharLit_c(Position pos, char value) {
        super(pos, value);
    }

    /** Get the value of the expression. */
    @Override
    public char value() {
        return (char) longValue();
    }

    /** Set the value of the expression. */
    @Override
    public CharLit value(char value) {
        CharLit_c n = (CharLit_c) copy();
        n.value = value;
        return n;
    }

    /** Type check the expression. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return type(tc.typeSystem().Char());
    }

    @Override
    public String toString() {
        return "'" + StringUtil.escape((char) value) + "'";
    }

    /** Write the expression to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("'");
        w.write(StringUtil.escape((char) value));
        w.write("'");
    }

    @Override
    public Object constantValue() {
        return new Character((char) value);
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.CharLit(this.position, (char) this.value);
    }

}

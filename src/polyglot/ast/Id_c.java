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

import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.util.StringUtil;
import polyglot.visit.PrettyPrinter;

/**
 * An <code>AmbExpr</code> is an ambiguous AST node composed of a single
 * identifier that must resolve to an expression.
 */
public class Id_c extends Node_c implements Id {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected String id;

    public Id_c(Position pos, String id) {
        super(pos);
        assert (id != null);
        assert (StringUtil.isNameShort(id));
        this.id = id;
    }

    // Override to make Name.equals(String) a compile-time error
    public final void equals(String s) {
    }

    /** Get the name of the expression. */
    @Override
    public String id() {
        return this.id;
    }

    /** Set the name of the expression. */
    @Override
    public Id id(String id) {
        Id_c n = (Id_c) copy();
        n.id = id;
        return n;
    }

    /** Write the name to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(id);
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public void dump(CodeWriter w) {
        w.write("(Id \"" + id + "\")");
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Id(this.position, this.id);
    }

}

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
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/** 
 * A <code>FloatLit</code> represents a literal in java of type
 * <code>float</code> or <code>double</code>.
 */
public class FloatLit_c extends Lit_c implements FloatLit {
    protected FloatLit.Kind kind;
    protected double value;

    public FloatLit_c(Position pos, FloatLit.Kind kind, double value) {
        super(pos);
        assert (kind != null);
        this.kind = kind;
        this.value = value;
    }

    /** Get the kind of the literal. */
    @Override
    public FloatLit.Kind kind() {
        return this.kind;
    }

    /** Set the kind of the literal. */
    @Override
    public FloatLit kind(FloatLit.Kind kind) {
        FloatLit_c n = (FloatLit_c) copy();
        n.kind = kind;
        return n;
    }

    /** Get the value of the expression. */
    @Override
    public double value() {
        return this.value;
    }

    /** Set the value of the expression. */
    @Override
    public FloatLit value(double value) {
        FloatLit_c n = (FloatLit_c) copy();
        n.value = value;
        return n;
    }

    /** Type check the expression. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        if (kind == FLOAT) {
            return type(tc.typeSystem().Float());
        }
        else if (kind == DOUBLE) {
            return type(tc.typeSystem().Double());
        }
        else {
            throw new InternalCompilerError("Unrecognized FloatLit kind "
                    + kind);
        }
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    /** Write the expression to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (kind == FLOAT) {
            w.write(Float.toString((float) value) + "F");
        }
        else if (kind == DOUBLE) {
            w.write(Double.toString(value));
        }
        else {
            throw new InternalCompilerError("Unrecognized FloatLit kind "
                    + kind);
        }
    }

    @Override
    public Object constantValue() {
        if (kind == FLOAT) {
            return new Float(value);
        }
        else {
            return new Double(value);
        }
    }

    @Override
    public Precedence precedence() {
        if (value < 0) {
            return Precedence.UNARY;
        }
        else {
            return Precedence.LITERAL;
        }
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.FloatLit(this.position, this.kind, this.value);
    }

}

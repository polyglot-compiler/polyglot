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
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/** 
 * A {@code FloatLit} represents a literal in java of type
 * {@code float} or {@code double}.
 */
public class FloatLit_c extends Lit_c implements FloatLit {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected FloatLit.Kind kind;
    protected double value;

//    @Deprecated
    public FloatLit_c(Position pos, FloatLit.Kind kind, double value) {
        this(pos, kind, value, null);
    }

    public FloatLit_c(Position pos, FloatLit.Kind kind, double value, Ext ext) {
        super(pos, ext);
        assert (kind != null);
        this.kind = kind;
        this.value = value;
    }

    @Override
    public FloatLit.Kind kind() {
        return this.kind;
    }

    @Override
    public FloatLit kind(FloatLit.Kind kind) {
        return kind(this, kind);
    }

    protected <N extends FloatLit_c> N kind(N n, FloatLit.Kind kind) {
        if (n.kind == kind) return n;
        n = copyIfNeeded(n);
        n.kind = kind;
        return n;
    }

    @Override
    public double value() {
        return this.value;
    }

    @Override
    public FloatLit value(double value) {
        return value(this, value);
    }

    protected <N extends FloatLit_c> N value(N n, double value) {
        if (n.value == value) return n;
        n = copyIfNeeded(n);
        n.value = value;
        return n;
    }

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
    public Object constantValue(Lang lang) {
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

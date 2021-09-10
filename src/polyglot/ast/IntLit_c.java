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
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An {@code IntLit} represents a literal in Java of an integer
 * type.
 */
public class IntLit_c extends NumLit_c implements IntLit {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /** The kind of literal: INT or LONG. */
    protected Kind kind;

    //    @Deprecated
    public IntLit_c(Position pos, Kind kind, long value) {
        this(pos, kind, value, null);
    }

    public IntLit_c(Position pos, Kind kind, long value, Ext ext) {
        super(pos, value, ext);
        assert (kind != null);
        this.kind = kind;
    }

    @Override
    public boolean boundary() {
        return (kind == INT && (int) value == Integer.MIN_VALUE)
                || (kind == LONG && value == Long.MIN_VALUE);
    }

    @Override
    public long value() {
        return longValue();
    }

    @Override
    public IntLit value(long value) {
        return value(this, value);
    }

    protected <N extends IntLit_c> N value(N n, long value) {
        if (n.value == value) return n;
        n = copyIfNeeded(n);
        n.value = value;
        return n;
    }

    @Override
    public IntLit.Kind kind() {
        return kind;
    }

    @Override
    public IntLit kind(IntLit.Kind kind) {
        return kind(this, kind);
    }

    protected <N extends IntLit_c> N kind(N n, IntLit.Kind kind) {
        if (n.kind == kind) return n;
        n = copyIfNeeded(n);
        n.kind = kind;
        return n;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        Kind kind = kind();

        if (kind == INT) {
            return type(ts.Int());
        } else if (kind == LONG) {
            return type(ts.Long());
        } else {
            throw new InternalCompilerError("Unrecognized IntLit kind " + kind);
        }
    }

    @Override
    public String positiveToString() {
        if (kind() == LONG) {
            if (boundary()) {
                // the literal is negative, but print it as positive.
                return "9223372036854775808L";
            } else if (value < 0) {
                return "0x" + Long.toHexString(value) + "L";
            } else {
                return Long.toString(value) + "L";
            }
        } else {
            if (boundary()) {
                // the literal is negative, but print it as positive.
                return "2147483648";
            } else if ((int) value < 0) {
                return "0x" + Integer.toHexString((int) value);
            } else {
                return Integer.toString((int) value);
            }
        }
    }

    @Override
    public String toString() {
        if (kind() == LONG) {
            return Long.toString(value) + "L";
        } else {
            return Long.toString((int) value);
        }
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(toString());
    }

    @Override
    public Object constantValue(Lang lang) {
        if (kind() == LONG) {
            return new Long(value);
        } else {
            return new Integer((int) value);
        }
    }

    @Override
    public Precedence precedence() {
        if (value < 0L && !boundary()) {
            return Precedence.UNARY;
        } else {
            return Precedence.LITERAL;
        }
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.IntLit(this.position, this.kind, this.value);
    }
}

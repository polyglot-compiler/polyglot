/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2007 Polyglot project group, Cornell University
 * Copyright (c) 2006-2007 IBM Corporation
 * 
 */

package polyglot.ast;

import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

/**
 * An <code>IntLit</code> represents a literal in Java of an integer
 * type.
 */
public class IntLit_c extends NumLit_c implements IntLit
{
    /** The kind of literal: INT or LONG. */ 
    protected Kind kind;

    public IntLit_c(Position pos, Kind kind, long value) {
	super(pos, value);
	assert(kind != null);
        this.kind = kind;
    }

    /**
     * @return True if this is a boundary case: the literal can only appear
     * as the operand of a unary minus.
     */
    public boolean boundary() {
        return (kind == INT && (int) value == Integer.MIN_VALUE)
            || (kind == LONG && value == Long.MIN_VALUE);
    }

    /** Get the value of the expression. */
    public long value() {
        return longValue();
    }

    /** Set the value of the expression. */
    public IntLit value(long value) {
        IntLit_c n = (IntLit_c) copy();
	n.value = value;
	return n;
    }

    /** Get the kind of the expression. */
    public IntLit.Kind kind() {
        return kind;
    }

    /** Set the kind of the expression. */
    public IntLit kind(IntLit.Kind kind) {
	IntLit_c n = (IntLit_c) copy();
	n.kind = kind;
	return n;
    }

    /** Type check the expression. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

	Kind kind = kind();

        if (kind == INT) {
	    return type(ts.Int());
	}
	else if (kind == LONG) {
	    return type(ts.Long());
	}
	else {
	    throw new InternalCompilerError("Unrecognized IntLit kind " + kind);
	}
    }

    public String positiveToString() {
	if (kind() == LONG) {
            if (boundary()) {
                // the literal is negative, but print it as positive.
                return "9223372036854775808L";
            }
            else if (value < 0) {
                return "0x" + Long.toHexString(value) + "L";
            }
            else {
                return Long.toString(value) + "L";
            }
	}
	else {
            if (boundary()) {
                // the literal is negative, but print it as positive.
                return "2147483648";
            }
            else if ((int) value < 0) {
                return "0x" + Integer.toHexString((int) value);
            }
            else {
                return Integer.toString((int) value);
            }
	}
    }

    public String toString() {
	if (kind() == LONG) {
            return Long.toString(value) + "L";
	}
	else {
            return Long.toString((int) value);
	}
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(toString());
    }

    public Object constantValue() {
	if (kind() == LONG) {
            return new Long(value);
	}
	else {
            return new Integer((int) value);
	}
    }

    public Precedence precedence() {
        if (value < 0L && ! boundary()) {
            return Precedence.UNARY;
        }
        else {
            return Precedence.LITERAL;
        }
    }
}

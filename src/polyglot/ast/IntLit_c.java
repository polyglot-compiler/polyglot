package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

/**
 * An <code>IntLit</code> represents a literal in Java of an integer
 * type.
 */
public class IntLit_c extends NumLit_c implements IntLit
{
    protected Kind kind;

    public IntLit_c(Position pos, Kind kind, long value) {
	super(pos, value);
        this.kind = kind;
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

    public String toString() {
	if (kind() == LONG) {
            return Long.toString(value) + "L";
        }
        else {
            return Long.toString(value);
        }
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	if (kind() == LONG) {
	    w.write(Long.toString(value) + "L");
	}
	else {
	    w.write(Long.toString(value));
	}
    }

    public boolean isConstant() {
      return true;
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
        if (value < 0) {
            return Precedence.UNARY;
        }
        else {
            return Precedence.LITERAL;
        }
    }
}

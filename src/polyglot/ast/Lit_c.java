package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

/**
 * <code>Lit</code> represents any Java literal.
 */
public abstract class Lit_c extends Expr_c implements Lit
{
    public Lit_c(Del ext, Position pos) {
	super(ext, pos);
    }

    public abstract Object objValue();

    /** Get the precedence of the expression. */
    public Precedence precedence() {
        return Precedence.LITERAL;
    }
}

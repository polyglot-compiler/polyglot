package jltools.ast;

import jltools.util.Enum;

/**
 * A <code>Unary</code> represents a Java unary expression, an
 * immutable pair of an expression and an an operator.
 */
public interface Unary extends Expr 
{
    public static class Operator extends Enum {
	boolean prefix;

        protected Operator(String name, boolean prefix) {
	    super(name);
	    this.prefix = prefix;
	}

	public boolean isPrefix() { return prefix; }
    }

    public static final Operator BIT_NOT  = new Operator("~", true);
    public static final Operator NEG      = new Operator("-", true);
    public static final Operator POST_INC = new Operator("++", false);
    public static final Operator POST_DEC = new Operator("--", false);
    public static final Operator PRE_INC  = new Operator("++", true);
    public static final Operator PRE_DEC  = new Operator("--", true);
    public static final Operator POS      = new Operator("+", true);
    public static final Operator NOT      = new Operator("!", true);

    Expr expr();
    Unary expr(Expr e);

    Operator operator();
    Unary operator(Operator o);
}

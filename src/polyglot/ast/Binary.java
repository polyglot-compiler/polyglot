package jltools.ast;

import jltools.util.Enum;

/**
 * A <code>Binary</code> represents a Java binary expression, an
 * immutable pair of expressions combined with an operator.
 */
public interface Binary extends Expr 
{
    public static class Operator extends Enum {
	protected Precedence prec;

        protected Operator(String name, Precedence prec) {
	    super(name);
	    this.prec = prec;
	}

	public Precedence precedence() { return prec; }
    }

    public static final Operator GT       = new Operator(">", Precedence.RELATIONAL);
    public static final Operator LT       = new Operator("<", Precedence.RELATIONAL);
    public static final Operator EQ       = new Operator("==", Precedence.EQUAL);
    public static final Operator LE       = new Operator("<=", Precedence.RELATIONAL);
    public static final Operator GE       = new Operator(">=", Precedence.RELATIONAL);
    public static final Operator NE       = new Operator("!=", Precedence.EQUAL);
    public static final Operator COND_OR  = new Operator("||", Precedence.COND_OR);
    public static final Operator COND_AND = new Operator("&&", Precedence.COND_AND);
    public static final Operator ADD      = new Operator("+", Precedence.ADD);
    public static final Operator SUB      = new Operator("-", Precedence.ADD);
    public static final Operator MUL      = new Operator("*", Precedence.MUL);
    public static final Operator DIV      = new Operator("/", Precedence.MUL);
    public static final Operator MOD      = new Operator("%", Precedence.MUL);
    public static final Operator BIT_OR   = new Operator("|", Precedence.BIT_OR);
    public static final Operator BIT_AND  = new Operator("&", Precedence.BIT_OR);
    public static final Operator BIT_XOR  = new Operator("^", Precedence.BIT_XOR);
    public static final Operator SHL      = new Operator("<<", Precedence.SHIFT);
    public static final Operator SHR      = new Operator(">>", Precedence.SHIFT);
    public static final Operator USHR     = new Operator(">>>", Precedence.SHIFT);

    Expr left();
    Binary left(Expr left);

    Operator operator();
    Binary operator(Operator op);

    Expr right();
    Binary right(Expr right);

    boolean throwsArithmeticException();
}

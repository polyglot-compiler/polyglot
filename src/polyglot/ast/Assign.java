package jltools.ast;

import jltools.util.Enum;

/**
 * A <code>Assign</code> represents a Java assignment expression.
 */
public interface Assign extends Expr 
{
    public static class Operator extends Enum {
	protected Operator(String name) { super(name); } 
    }

    public static final Operator ASSIGN         = new Operator("=");
    public static final Operator ADD_ASSIGN     = new Operator("+=");
    public static final Operator SUB_ASSIGN     = new Operator("-=");
    public static final Operator MUL_ASSIGN     = new Operator("*=");
    public static final Operator DIV_ASSIGN     = new Operator("/=");
    public static final Operator MOD_ASSIGN     = new Operator("%=");
    public static final Operator BIT_AND_ASSIGN = new Operator("&=");
    public static final Operator BIT_OR_ASSIGN  = new Operator("|=");
    public static final Operator BIT_XOR_ASSIGN = new Operator("^=");
    public static final Operator SHL_ASSIGN     = new Operator("<<=");
    public static final Operator SHR_ASSIGN     = new Operator(">>=");
    public static final Operator USHR_ASSIGN    = new Operator(">>>=");

    Expr left();
    Assign left(Expr left);

    Operator operator();
    Assign operator(Operator op);

    Expr right();
    Assign right(Expr right);

    boolean throwsArithmeticException();
}

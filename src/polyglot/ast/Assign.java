package polyglot.ast;

import polyglot.util.Enum;

/**
 * An <code>Assign</code> represents a Java assignment expression.
 */
public interface Assign extends Expr, Thrower
{
    /** Assignment operator. */
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

    /**
     * Left child (target) of the assignment.
     */
    Expr left();

    /**
     * Set the left child (target) of the assignment.
     */
    Assign left(Expr left);

    /**
     * The assignment's operator.
     */
    Operator operator();

    /**
     * Set the assignment's operator.
     */
    Assign operator(Operator op);

    /**
     * Right child (source) of the assignment.
     */
    Expr right();

    /**
     * Set the right child (source) of the assignment.
     */
    Assign right(Expr right);

    /**
     * Returns true if the assignment might throw an arithmetic exception,
     * such as division by zero.
     */
    boolean throwsArithmeticException();
}

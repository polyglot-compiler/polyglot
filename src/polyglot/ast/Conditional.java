package polyglot.ast;

/**
 * A <code>Conditional</code> is a representation of a Java ternary
 * expression <code>(cond ? consequent : alternative)</code>.
 */
public interface Conditional extends Expr 
{
    Expr cond();
    Conditional cond(Expr cond);

    Expr consequent();
    Conditional consequent(Expr consequent);

    Expr alternative();
    Conditional alternative(Expr alternative);
}

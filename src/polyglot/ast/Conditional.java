package jltools.ast;

/**
 * A <code>Conditional</code> represents a Java ternary expression as
 * an immutable triple of expressions.
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

package jltools.ast;

/**
 * An immutable representation of a Java language <code>if</code> statement.
 * Contains an expression whose value is tested, a ``then'' statement 
 * (consequent), and optionally an ``else'' statement (alternate).
 */
public interface If extends Stmt 
{
    Expr cond();
    If cond(Expr cond);

    Stmt consequent();
    If consequent(Stmt consequent);

    Stmt alternative();
    If alternative(Stmt alternative);
}

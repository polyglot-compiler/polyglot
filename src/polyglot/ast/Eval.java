package jltools.ast;

/**
 * An <code>Eval</code> is a wrapper for an expression in the context of
 * a statement.
 */
public interface Eval extends ForInit, ForUpdate
{
    Expr expr();
    Eval expr(Expr expr);
}

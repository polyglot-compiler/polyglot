package jltools.ast;

/**
 * <code>Lit</code> represents any Java literal.
 */
public interface Lit extends Expr
{
    Object objValue();
}

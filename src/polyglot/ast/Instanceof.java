package jltools.ast;

/**
 * An <code>Instanceof</code> is an immutable representation of
 * the use of the <code>instanceof</code> operator.
 */
public interface Instanceof extends Expr 
{
    Expr expr();
    Instanceof expr(Expr expr);

    TypeNode compareType();
    Instanceof compareType(TypeNode compareType);
}

package polyglot.ast;

/**
 * A <code>Cast</code> is an immutable representation of a casting
 * operation.  It consists of an <code>Expr</code> being cast and a
 * <code>TypeNode</code> being cast to.
 */ 
public interface Cast extends Expr
{
    TypeNode castType();
    Cast castType(TypeNode castType);

    Expr expr();
    Cast expr(Expr expr);
}

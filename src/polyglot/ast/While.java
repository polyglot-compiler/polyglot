package jltools.ast;

/**
 * An immutable representation of a Java language <code>while</code>
 * statement.  It contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */ 
public interface While extends Stmt 
{
    Expr cond();
    While cond(Expr cond);

    Stmt body();
    While body(Stmt body);
}

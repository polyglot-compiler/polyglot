package jltools.ast;

/**
 * A immutable representation of a Java language <code>do</code> statement. 
 * It contains a statement to be executed and an expression to be tested 
 * indicating whether to reexecute the statement.
 */ 
public interface Do extends Stmt 
{
    Stmt body();
    Do body(Stmt body);

    Expr cond();
    Do cond(Expr cond);
}

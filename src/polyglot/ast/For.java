package polyglot.ast;

import java.util.*;

/**
 * An immutable representation of a Java language <code>for</code>
 * statement.  Contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */
public interface For extends Stmt 
{    
    /** List of initialization statements */
    List inits();
    /** Set the list of initialization statements */
    For inits(List inits);

    /** Loop condition */
    Expr cond();
    /** Set the loop condition */
    For cond(Expr cond);

    /** List of iterator expressions */
    List iters();
    /** Set the list of iterator expressions */
    For iters(List iters);

    /** Loop body */
    Stmt body();
    /** Set the loop body */
    For body(Stmt body);
}

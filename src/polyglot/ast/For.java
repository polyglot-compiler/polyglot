package polyglot.ast;

import java.util.*;

/**
 * An immutable representation of a Java language <code>for</code>
 * statement.  Contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */
public interface For extends Stmt 
{    
    /** List of initialization statements.
     * A list of <code>ForInit</code>.
     * @see polyglot.ast.ForInit
     */
    List inits();

    /** Set the list of initialization statements.
     * A list of <code>ForInit</code>.
     * @see polyglot.ast.ForInit
     */
    For inits(List inits);

    /** Loop condition */
    Expr cond();

    /** Set the loop condition */
    For cond(Expr cond);

    /** List of iterator expressions.
     * A list of <code>ForUpdate</code>.
     * @see polyglot.ast.ForUpdate
     */
    List iters();

    /** Set the list of iterator expressions.
     * A list of <code>ForUpdate</code>.
     * @see polyglot.ast.ForUpdate
     */
    For iters(List iters);

    /** Loop body */
    Stmt body();

    /** Set the loop body */
    For body(Stmt body);
}

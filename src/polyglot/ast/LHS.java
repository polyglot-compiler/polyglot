package polyglot.ast;

import polyglot.visit.*;

/** 
 * A local variable expression.
 */
public interface LHS extends Expr
{
    public void visitAssignCFG(Assign assign, CFGBuilder v);
    public Term lhsEntry(Assign assign);
}

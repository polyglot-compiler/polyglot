package polyglot.visit;

import polyglot.ast.NodeFactory;
import polyglot.frontend.goals.Goal;
import polyglot.types.TypeSystem;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public class DisambiguationDriver extends ContextVisitor
{
    public DisambiguationDriver(Goal goal, TypeSystem ts, NodeFactory nf) {
        super(goal, ts, nf);
    }
}

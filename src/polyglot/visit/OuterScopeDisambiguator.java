package polyglot.visit;

import polyglot.ast.TopLevelDecl;
import polyglot.ast.Node;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public class OuterScopeDisambiguator extends AmbiguityRemover
{
    public OuterScopeDisambiguator(DisambiguationDriver dd) {
        super(dd);
    }
    
    public Node override(Node parent, Node n) {
        // Only visit imports and package declarations.
        if (n instanceof TopLevelDecl) {
            return n;
        }
        return null;
    }
}

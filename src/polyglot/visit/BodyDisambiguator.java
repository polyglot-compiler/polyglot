package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.Context;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public class BodyDisambiguator extends Disambiguator
{
    public BodyDisambiguator(DisambiguationDriver dd) {
        super(dd);;
    }

    public Node override(Node parent, Node n) {
        Context c = this.context();
        if (n instanceof ClassDecl && ! ((ClassDecl) n).type().isMember()) {
            // Will be invoked by ComputeTypesVisitor.override.
            return n;
        }
        return super.override(parent, n);
    }
}

package polyglot.visit;

import polyglot.ast.*;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public class SignatureDisambiguator extends Disambiguator
{
    public SignatureDisambiguator(DisambiguationDriver dd) {
        super(dd);
    }

    public Node override(Node parent, Node n) {
        if (n instanceof Stmt || n instanceof Expr) {
            return n;
        }
        return super.override(parent, n);
    }
}

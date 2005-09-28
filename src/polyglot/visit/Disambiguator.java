package polyglot.visit;

import polyglot.ast.*;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.types.*;
import polyglot.util.*;

/** Visitor which performs type checking on the AST. */
public class Disambiguator extends AmbiguityRemover
{
    DisambiguationDriver dd;
 
    public Disambiguator(DisambiguationDriver dd) {
        super(dd.job(), dd.typeSystem(), dd.nodeFactory());
        this.dd = dd;
        this.context = dd.context();
    }

    public NodeVisitor begin() {
        Disambiguator v = (Disambiguator) super.begin();
        v.context = dd.context();
        return v;
    }
//    
//    public Node override(Node parent, Node n) {
//        return null;
//    }
}

package polyglot.visit;

import polyglot.ast.Ambiguous;
import polyglot.ast.Node;
import polyglot.main.Report;
import polyglot.types.SemanticException;
import polyglot.types.UnavailableTypeException;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public abstract class AmbiguityRemover extends ContextVisitor
{
    DisambiguationDriver dd;
    
    public AmbiguityRemover(DisambiguationDriver dd) {
        super(dd.goal(), dd.typeSystem(), dd.nodeFactory());
        this.dd = dd;
        this.context = dd.context();
    }

    public NodeVisitor begin() {
        AmbiguityRemover v = (AmbiguityRemover) super.begin();
        v.context = dd.context();
        return v;
    }

    protected NodeVisitor enterCall(Node n) throws SemanticException {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::enter " + n);
        
        AmbiguityRemover v = (AmbiguityRemover) n.del().disambiguateEnter(this);
        
        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::enter " + n + " -> " + v);
        
        return v;
    }
    
    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::leave " + n);
        
        Node m = n;
        
        try {
            m = n.del().disambiguate((AmbiguityRemover) v);
        }
        catch (UnavailableTypeException e) {
            // ignore: we'll revisit the pass later
        }
        
        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::leave " + n + " -> " + m);
        
        return m;
    }
}

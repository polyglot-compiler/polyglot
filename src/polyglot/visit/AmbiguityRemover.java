package polyglot.visit;

import polyglot.ast.*;
import polyglot.ast.Ambiguous;
import polyglot.ast.Node;
import polyglot.main.Report;
import polyglot.types.SemanticException;
import polyglot.types.UnavailableTypeException;
import polyglot.util.*;
import polyglot.util.ErrorInfo;
import polyglot.util.Position;

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
    
    public Node override(Node parent, Node n) {
        try {
            if (Report.should_report(Report.visit, 2))
                Report.report(2, ">> " + this + "::override " + n);
            
            Node m = n.disambiguateOverride(this);
            
            if (Report.should_report(Report.visit, 2))
                Report.report(2, "<< " + this + "::override " + n + " -> " + m);
            
            return m;
        }
        catch (SemanticException e) {
            if (e.getMessage() != null) {
                Position position = e.position();
                
                if (position == null) {
                    position = n.position();
                }
                
                this.errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
                                     e.getMessage(), position);
            }
            else {
                // silent error; these should be thrown only
                // when the error has already been reported 
            }
            
            return n;
        }    
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

        final boolean[] amb = new boolean[1];
        
        n.visitChildren(new NodeVisitor() {
            public Node override(Node n) {
                if (n instanceof Ambiguous) {
                    amb[0] = true;
                }
                return n;
            }
        });
        
        Node m = n;
        
        try {
            if (! amb[0]) {
                m = m.del().disambiguate((AmbiguityRemover) v);
            }
        }
        catch (UnavailableTypeException e) {
            // ignore: we'll rerun the pass later
        }
        
        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::leave " + n + " -> " + m);
        
        return m;
    }  
}

package polyglot.visit;

import polyglot.ast.*;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.types.*;
import polyglot.util.*;

/** Visitor which performs type checking on the AST. */
public class TypeChecker extends DisambiguationDriver
{
    public TypeChecker(Goal goal, TypeSystem ts, NodeFactory nf) {
        super(goal, ts, nf);
    }
   
    public Node override(Node parent, Node n) {
        try {
            if (Report.should_report(Report.visit, 2))
                Report.report(2, ">> " + this + "::override " + n);
            
            Node m = n.typeCheckOverride(parent, this);
            
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
        
        TypeChecker v = (TypeChecker) n.del().typeCheckEnter(this);
        
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
                if (! n.isCanonical()) {
                    amb[0] = true;
                }
                return n;
            }
        });
        
        Node m = n;
        
        try {
            if (! amb[0]) {
                m = m.del().typeCheck((TypeChecker) v);
                
                if (m instanceof Expr && ((Expr) m).type() == null) {
                    throw new InternalCompilerError("Null type for " + m, m.position());
                }
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

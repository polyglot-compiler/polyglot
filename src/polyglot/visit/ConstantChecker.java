package polyglot.visit;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.types.*;

/** Visitor which performs type checking on the AST. */
public class ConstantChecker extends DisambiguationDriver
{
    public ConstantChecker(Goal goal, TypeSystem ts, NodeFactory nf) {
        super(goal, ts, nf);
    }
    
    /*
    protected NodeVisitor enterCall(Node n) throws SemanticException {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::enter " + n);
        
        ConstantChecker v = (ConstantChecker) n.del().checkConstantsEnter(this);
        
        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::enter " + n + " -> " + v);
        
        return v;
    }
    */
    
    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::leave " + n);
        
        Node m = n;
        
        try {
            m = m.del().checkConstants((ConstantChecker) v);
        }
        catch (UnavailableTypeException e) {
            // ignore: we'll rerun the pass later
        }
        
        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::leave " + n + " -> " + m);
        
        return m;
    }   
}

package polyglot.visit;

import java.util.Iterator;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;

/** Visitor which performs type checking on the AST. */
public class ConstantChecker extends ContextVisitor
{
    public ConstantChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
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
        
        final boolean[] typeChecked = new boolean[1];
        
        if (n instanceof Expr) {
            Expr e = (Expr) n;
            if (e.type() == null || ! e.type().isCanonical()) {
                typeChecked[0] = true;
            }
        }

        if (! typeChecked[0]) {
            n.visitChildren(new NodeVisitor() {
                public Node override(Node n) {    
                    if (n instanceof Expr &&
                        (((Expr) n).type() == null || ! ((Expr) n).type().isCanonical())) {
                        typeChecked[0] = true;
                    }
                    return n;
                }
            });
        }
        
        Node m = n;
        
        if (! typeChecked[0]) {
            m = m.del().checkConstants((ConstantChecker) v);
        }
        else {
            Scheduler scheduler = job().extensionInfo().scheduler();
            Goal g = scheduler.TypeChecked(job());
            throw new MissingDependencyException(g);
        }
            
        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::leave " + n + " -> " + m);
        
        return m;
    }   
}

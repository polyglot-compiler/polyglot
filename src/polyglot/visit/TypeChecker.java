package polyglot.visit;

import java.util.Iterator;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.frontend.Job;
import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.*;

/** Visitor which performs type checking on the AST. */
public class TypeChecker extends DisambiguationDriver
{
    public TypeChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }
   
    public Node override(Node parent, Node n) {
        try {
            if (Report.should_report(Report.visit, 2))
                Report.report(2, ">> " + this + "::override " + n);
            
            Node m = n.del().typeCheckOverride(parent, this);
            
            if (Report.should_report(Report.visit, 2))
                Report.report(2, "<< " + this + "::override " + n + " -> " + m);
            
            return m;
        }
        catch (MissingDependencyException e) {
            if (Report.should_report(Report.frontend, 3))
                e.printStackTrace();
            Scheduler scheduler = job.extensionInfo().scheduler();
            Goal g = scheduler.currentGoal();
            scheduler.addDependencyAndEnqueue(g, e.goal(), e.prerequisite());
            g.setUnreachableThisRun();
            return n;
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
                if (! n.isDisambiguated() || ! n.isTypeChecked()) {
//                    System.out.println("  !!!!! no type at " + n + " (" + n.getClass().getName() + ")");
//                    if (n instanceof Expr)  
//                        System.out.println("   !!!! n.type = " + ((Expr) n).type());
                    amb[0] = true;
                }
                return n;
            }
        });
        
        Node m = n;
        
        if (! amb[0] && m.isDisambiguated()) {
//          System.out.println("running typeCheck for " + m);
            m = m.del().typeCheck((TypeChecker) v);
            
//            if (! m.isTypeChecked()) {
//                throw new InternalCompilerError("Type checking failed for " + m + " (" + m.getClass().getName() + ")", m.position());
//            }
        }
        else {
//                 System.out.println("  no type at " + m);
            Goal g = job.extensionInfo().scheduler().currentGoal();
            g.setUnreachableThisRun();
        }
        
        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::leave " + n + " -> " + m);
        
        return m;
    }   
}

package polyglot.visit;

import java.util.*;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.frontend.Job;
import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.types.*;
import polyglot.util.*;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public class AmbiguityRemover extends DisambiguationDriver
{
    boolean visitSigs;
    boolean visitBodies;
    
    public AmbiguityRemover(Job job, TypeSystem ts, NodeFactory nf) {
        this(job, ts, nf, true, true);
    }
    
    public AmbiguityRemover(Job job, TypeSystem ts, NodeFactory nf, boolean visitSigs, boolean visitBodies) {
        super(job, ts, nf);
        this.visitSigs = visitSigs;
        this.visitBodies = visitBodies;
    }
    
    protected Context enterScope(Node parent, Node n) {
        Context c = super.enterScope(parent, n);
        
        Scheduler scheduler = job.extensionInfo().scheduler();
        ParsedClassType ct = c.currentClassScope();
        
        if (parent instanceof ClassMember && (n instanceof Stmt || n instanceof Expr)) {
            List l = new ArrayList(c.goalStack());
            l.remove(scheduler.SupertypesResolved(ct));
            l.remove(scheduler.SignaturesResolved(ct));
            c = c.pushGoalStack(l);
        }
        else if (n instanceof FieldDecl || n instanceof MethodDecl || n instanceof ConstructorDecl) {
            c = c.pushGoal(scheduler.SignaturesResolved(ct));
        }
        else if (n instanceof ClassDecl) {
            c = c.pushGoal(scheduler.SupertypesResolved(((ClassDecl) n).type()));
        }
        else if (n instanceof ClassBody) {
            List l = new ArrayList(c.goalStack());
            l.remove(scheduler.SupertypesResolved(ct));
            l.remove(scheduler.SignaturesResolved(ct));
            c = c.pushGoalStack(l);
        }
        
        return c;
    }

    public Node override(Node parent, Node n) {
        if (! visitSigs && n instanceof ClassMember && ! (n instanceof ClassDecl)) {
            return n;
        }
        if ((! visitBodies || ! visitSigs) && (n instanceof Expr || n instanceof Stmt)) {
            return n;
        }
        
        try {
            if (Report.should_report(Report.visit, 2))
                Report.report(2, ">> " + this + "::override " + n + " (" + n.getClass().getName() + ")");
            
            Node m = n.del().disambiguateOverride(parent, this);
            
            if (Report.should_report(Report.visit, 2))
                Report.report(2, "<< " + this + "::override " + n + " -> " + m + (m != null ? (" (" + m.getClass().getName() + ")") : ""));
            
            return m;
        }
        catch (MissingDependencyException e) {
            Scheduler scheduler = job.extensionInfo().scheduler();
            for (Iterator i = context.goalStack().iterator(); i.hasNext(); ) {
                Goal g = (Goal) i.next();
                if (Report.should_report(Report.frontend, 3))
                    e.printStackTrace();
                scheduler.addDependencyAndEnqueue(g, e.goal(), e.prerequisite());
                g.setUnreachableThisRun();
            }
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
            Report.report(2, ">> " + this + "::enter " + n + " (" + n.getClass().getName() + ")");
        
        AmbiguityRemover v = (AmbiguityRemover) n.del().disambiguateEnter(this);
        
        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::enter " + n+ " (" + n.getClass().getName() + ")" + " -> " + v);
        
        return v;
    }
    
    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::leave " + n + " (" + n.getClass().getName() + ")");

        final boolean[] amb = new boolean[1];
        
        n.visitChildren(new NodeVisitor() {
            public Node override(Node n) {
                if (n instanceof Ambiguous) {
            // System.out.println("ambiguous node " + n + " (" + n.getClass().getName() + ")");
                    amb[0] = true;
                }
                return n;
            }
        });
        
        Node m = n;
        
        if (! amb[0]) {
            m = m.del().disambiguate((AmbiguityRemover) v);
        }
        else {
            // System.out.println("ambiguous node at " + m + " (" + m.getClass().getName() + ")");
            for (Iterator i = context.goalStack().iterator(); i.hasNext(); ) {
                Goal g = (Goal) i.next();
                // System.out.println("  " + g + " unreachable");
                g.setUnreachableThisRun();
            }
        }
        
        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::leave " + n + " -> " + m + (m != null ? (" (" + m.getClass().getName() + ")") : ""));
        
        return m;
    }
    
    public HaltingVisitor bypass(Collection c) {
        throw new InternalCompilerError("AmbiguityRemover does not support bypassing. " +
                                        "Implement any required functionality using " +
                                        "Node.disambiguateOverride(Node, AmbiguityRemover).");
    }
    public HaltingVisitor bypass(Node n) {
        throw new InternalCompilerError("AmbiguityRemover does not support bypassing. " +
                                        "Implement any required functionality using " +
                                        "Node.disambiguateOverride(Node, AmbiguityRemover).");
    }
    public HaltingVisitor bypassChildren(Node n) {
        throw new InternalCompilerError("AmbiguityRemover does not support bypassing. " +
                                        "Implement any required functionality using " +
                                        "Node.disambiguateOverride(Node, AmbiguityRemover).");
    }
}

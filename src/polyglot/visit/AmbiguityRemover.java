package polyglot.visit;

import java.util.Iterator;
import java.util.Stack;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.Job;
import polyglot.main.Report;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public class AmbiguityRemover extends ContextVisitor
{
    public static class Kind extends Enum {
        protected Kind(String name) {
            super(name);
        }
    }

    public static final Kind SUPER = new Kind("disam-super");
    public static final Kind SIGNATURES = new Kind("disam-sigs");
    public static final Kind FIELDS = new Kind("disam-fields");
    public static final Kind ALL = new Kind("disam-all");

    protected Kind kind;

    public AmbiguityRemover(Job job, TypeSystem ts, NodeFactory nf, Kind kind) {
        super(job, ts, nf);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    protected NodeVisitor enterCall(Node n) throws SemanticException {
        if (Report.should_report(Report.visit, 2))
	    Report.report(2, ">> " + kind + "::enter " + n);
        NodeVisitor v = n.del().disambiguateEnter(this);
        if (Report.should_report(Report.visit, 2))
	    Report.report(2, "<< " + kind + "::enter " + n + " -> " + v);
        return v;
    }

    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (Report.should_report(Report.visit, 2))
	    Report.report(2, ">> " + kind + "::leave " + n);
        Node m = n.del().disambiguate((AmbiguityRemover) v);
        if (Report.should_report(Report.visit, 2))
	    Report.report(2, "<< " + kind + "::leave " + n + " -> " + m);
        return m;
    }
    
    /**
     * Add dependencies for the job to the super classes and interface classes
     * of <code>ct</code>.
     */
    public void addSuperDependencies(ClassType ct) {        
        Stack s = new Stack();
        s.push(ct);
        while (! s.isEmpty()) {
            Type t = (Type) s.pop();
            if (t.isClass()) {
                ClassType classt = t.toClass();
                // add a dependency if its a parsed class type.
                if (classt instanceof ParsedClassType) {
                    this.job().extensionInfo().addDependencyToCurrentJob(
                                      ((ParsedClassType)classt).fromSource());
                }
                
                // add all the interfaces to the stack.
                for (Iterator i = classt.interfaces().iterator(); i.hasNext(); ) {
                    s.push(i.next());
                }
    
                // add the superType to the stack.
                if (classt.superType() != null) {
                    s.push(classt.superType());
                }
            }
        }

    }
}

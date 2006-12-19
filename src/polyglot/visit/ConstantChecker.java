/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

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
    
    protected static class TypeCheckChecker extends NodeVisitor {
        public boolean checked = true;
        public Node override(Node n) {   
            if (! n.isTypeChecked()) {
                checked = false;
            }
            return n;
        }
    }
    
    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::leave " + n);
        
        TypeCheckChecker tcc = new TypeCheckChecker();
        
        if (n instanceof Expr) {
            Expr e = (Expr) n;
            if (! e.isTypeChecked()) {
                tcc.checked = false;
            }
        }

        if (tcc.checked) {
            n.del().visitChildren(tcc);
        }
        
        Node m = n;
        
        if (tcc.checked) {
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

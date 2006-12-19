/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.frontend;

import polyglot.ast.Node;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.SourceFileGoal;
import polyglot.main.Report;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;
import polyglot.visit.NodeVisitor;

/** A pass which runs a visitor. */
public class VisitorPass extends AbstractPass
{
    protected NodeVisitor v;

    public VisitorPass(Goal goal) {
	this(goal, null);
    }

    public VisitorPass(Goal goal, NodeVisitor v) {
        super(goal);
        this.v = v;
    }

    public void visitor(NodeVisitor v) {
	this.v = v;
    }

    public NodeVisitor visitor() {
	return v;
    }
  
    public boolean run() {
	Node ast = goal.job().ast();

	if (ast == null) {
	    throw new InternalCompilerError("Null AST: did the parser run?");
	}

        NodeVisitor v_ = v.begin();
        
        if (v_ != null) {
	    ErrorQueue q = goal.job().compiler().errorQueue();
	    int nErrsBefore = q.errorCount();

            if (Report.should_report(Report.frontend, 3))
                Report.report(3, "Running " + v_ + " on " + ast);

            ast = ast.visit(v_);
            v_.finish(ast);

            int nErrsAfter = q.errorCount();

            goal.job().ast(ast);

            if (nErrsBefore != nErrsAfter) {
                // because, if they're equal, no new errors occurred,
                // so the run was successful.
                return false;
            }
           
            return true;
        }

        return false;
    }
    
    public String name() {
        if (v != null)
            return v.toString();
        else 
            return super.name();
    }
}

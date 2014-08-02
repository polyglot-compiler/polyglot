/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.frontend;

import polyglot.ast.Lang;
import polyglot.ast.Node;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;
import polyglot.visit.NodeVisitor;

/** A pass which runs a visitor. */
public class VisitorPass extends AbstractPass {
    protected NodeVisitor v;

    public VisitorPass(Goal goal) {
        this(goal, null);
    }

    public VisitorPass(Goal goal, NodeVisitor v) {
        super(goal);
        this.v = v;
    }

    @Override
    public Lang lang() {
        return v.lang();
    }

    public void visitor(NodeVisitor v) {
        this.v = v;
    }

    public NodeVisitor visitor() {
        return v;
    }

    @Override
    public boolean run() {
        Node ast = goal.job().ast();

        if (ast == null) {
            throw new InternalCompilerError("Null AST for job " + goal.job()
                    + ": did the parser run?");
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

    @Override
    public String name() {
        if (v != null)
            return v.toString();
        else return super.name();
    }
}

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

package polyglot.visit;

import polyglot.ast.Expr;
import polyglot.ast.JLang;
import polyglot.ast.Lang;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;

/** Visitor which performs type checking on the AST. */
public class ConstantChecker extends ContextVisitor {
    public ConstantChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public JLang lang() {
        return (JLang) super.lang();
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

        public TypeCheckChecker(Lang lang) {
            super(lang);
        }

        @Override
        public Node override(Node n) {
            if (!n.isTypeChecked()) {
                checked = false;
            }
            return n;
        }
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::leave " + n);

        TypeCheckChecker tcc = new TypeCheckChecker(lang());

        if (n instanceof Expr) {
            Expr e = (Expr) n;
            if (!e.isTypeChecked()) {
                tcc.checked = false;
            }
        }

        if (tcc.checked) {
            lang().visitChildren(n, tcc);
        }

        Node m = n;

        if (tcc.checked) {
            m = lang().checkConstants(m, (ConstantChecker) v);
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

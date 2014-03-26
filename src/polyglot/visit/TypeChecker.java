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

import polyglot.ast.JLang;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorInfo;
import polyglot.util.Position;

/** Visitor which performs type checking on the AST. */
public class TypeChecker extends DisambiguationDriver {
    protected boolean checkConstants = true;

    public TypeChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public JLang lang() {
        return (JLang) super.lang();
    }

    public void setCheckConstants(boolean check) {
        this.checkConstants = check;
    }

    @Override
    public Node override(Node parent, Node n) {
        try {
            if (Report.should_report(Report.visit, 2))
                Report.report(2, ">> " + this + "::override " + n);

            Node m = lang().typeCheckOverride(n, parent, this);

            if (Report.should_report(Report.visit, 2))
                Report.report(2, "<< " + this + "::override " + n + " -> " + m);

            return m;
        }
        catch (MissingDependencyException e) {
            if (Report.should_report(Report.frontend, 3)) e.printStackTrace();
            Scheduler scheduler = job.extensionInfo().scheduler();
            Goal g = scheduler.currentGoal();
            scheduler.addDependencyAndEnqueue(g, e.goal(), e.prerequisite());
            g.setUnreachableThisRun();
            if (this.rethrowMissingDependencies) {
                throw e;
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
                                          e.getMessage(),
                                          position);
            }
            else {
                // silent error; these should be thrown only
                // when the error has already been reported 
            }

            return n;
        }
    }

    @Override
    protected NodeVisitor enterCall(Node n) throws SemanticException {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::enter " + n);

        TypeChecker v = (TypeChecker) lang().typeCheckEnter(n, this);

        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::enter " + n + " -> " + v);

        return v;
    }

    protected static class AmbChecker extends NodeVisitor {
        public boolean amb;

        public AmbChecker(JLang lang) {
            super(lang);
        }

        @Override
        public Node override(Node n) {
            if (!n.isDisambiguated() || !n.isTypeChecked()) {
//                System.out.println("  !!!!! no type at " + n + " (" + n.getClass().getName() + ")");
//                if (n instanceof Expr)  
//                    System.out.println("   !!!! n.type = " + ((Expr) n).type());
                amb = true;
            }
            return n;
        }
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::leave " + n);

        AmbChecker ac = new AmbChecker(lang());
        lang().visitChildren(n, ac);

        Node m = n;

        if (!ac.amb && m.isDisambiguated()) {
//          System.out.println("running typeCheck for " + m);
            TypeChecker childTc = (TypeChecker) v;
            m = lang().typeCheck(m, childTc);

            if (checkConstants) {
                ConstantChecker cc = new ConstantChecker(job, ts, nf);
                cc = (ConstantChecker) cc.context(childTc.context());
                m = lang().checkConstants(m, cc);
            }

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

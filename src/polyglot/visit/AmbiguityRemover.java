/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.visit;

import java.util.Arrays;
import java.util.Collection;

import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.CodeDecl;
import polyglot.ast.FieldDecl;
import polyglot.ast.New;
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
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public class AmbiguityRemover extends DisambiguationDriver {
    protected boolean visitSigs;
    protected boolean visitBodies;

    public AmbiguityRemover(Job job, TypeSystem ts, NodeFactory nf) {
        this(job, ts, nf, true, true);
    }

    public AmbiguityRemover(Job job, TypeSystem ts, NodeFactory nf,
            boolean visitSigs, boolean visitBodies) {
        super(job, ts, nf);
        this.visitSigs = visitSigs;
        this.visitBodies = visitBodies;
    }

    @Override
    public Node override(Node parent, Node n) {
        if (!visitSigs && n instanceof ClassMember && !(n instanceof ClassDecl)) {
            return n;
        }
        if ((!visitBodies || !visitSigs) && parent instanceof ClassMember) {
            if (parent instanceof FieldDecl && ((FieldDecl) parent).init() == n) {
                return n;
            }
            if (parent instanceof CodeDecl && ((CodeDecl) parent).body() == n) {
                return n;
            }
        }

        try {
            if (Report.should_report(Report.visit, 2))
                Report.report(2, ">> " + this + "::override " + n + " ("
                        + n.getClass().getName() + ")");

            Node m = n.del().disambiguateOverride(parent, this);

            if (Report.should_report(Report.visit, 2))
                Report.report(2, "<< "
                        + this
                        + "::override "
                        + n
                        + " -> "
                        + m
                        + (m != null ? (" (" + m.getClass().getName() + ")")
                                : ""));

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
            Report.report(2, ">> " + this + "::enter " + n + " ("
                    + n.getClass().getName() + ")");

        AmbiguityRemover v = (AmbiguityRemover) n.del().disambiguateEnter(this);

        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::enter " + n + " ("
                    + n.getClass().getName() + ")" + " -> " + v);

        return v;
    }

    protected static class AmbChecker2 extends NodeVisitor {
        public boolean amb;

        @Override
        public Node override(Node n) {
            if (!n.isDisambiguated()) {
                amb = true;
            }
            return n;
        }
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::leave " + n + " ("
                    + n.getClass().getName() + ")");

//        AmbChecker2 ac = new AmbChecker2();
//        n.visitChildren(ac);
//        if (ac.amb) {
//            Goal g = job.extensionInfo().scheduler().currentGoal();
//            g.setUnreachableThisRun();
//            return n;
//        }

        Node m = n.del().disambiguate((AmbiguityRemover) v);

        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::leave " + n + " -> " + m
                    + (m != null ? (" (" + m.getClass().getName() + ")") : ""));

        return m;
    }

    @Override
    public HaltingVisitor bypass(Collection<? extends Node> c) {
        throw new InternalCompilerError("AmbiguityRemover does not support bypassing. "
                + "Implement any required functionality using "
                + "Node.disambiguateOverride(Node, AmbiguityRemover).");
    }

    @Override
    public HaltingVisitor bypass(Node n) {
        throw new InternalCompilerError("AmbiguityRemover does not support bypassing. "
                + "Implement any required functionality using "
                + "Node.disambiguateOverride(Node, AmbiguityRemover).");
    }

    @Override
    public HaltingVisitor bypassChildren(Node n) {
        throw new InternalCompilerError("AmbiguityRemover does not support bypassing. "
                + "Implement any required functionality using "
                + "Node.disambiguateOverride(Node, AmbiguityRemover).");
    }

    public boolean isASTDisambiguated(Node n) {
        return astAmbiguityCount(n) == 0;
    }

    protected static class AmbChecker extends NodeVisitor {
        public int notOkCount;

        @Override
        public Node override(Node parent, Node n) {
            final Collection<String> TOPICS =
                    Arrays.asList(new String[] { Report.types, Report.frontend,
                            "disam-check" });

            // Don't check if New is disambiguated; this is handled
            // during type-checking.
            if (n instanceof New) {
                return n;
            }

            if (!n.isDisambiguated()) {
                if (Report.should_report(TOPICS, 3))
                    Report.report(3, "  not ok at " + n + " ("
                            + n.getClass().getName() + ")");
                notOkCount++;
            }

            return null;
        }
    }

    public static int astAmbiguityCount(Node n) {
        AmbChecker ac = new AmbChecker();
        n.visit(ac);
        return ac.notOkCount;
    }
}

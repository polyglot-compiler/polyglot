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

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

/**
 * A visitor which maintains a context throughout the visitor's pass.  This is
 * the base class of the disambiguation and type checking visitors.
 *
 * TODO: update this documentation.
 * For a node {@code n} methods are called in this order:
 * <pre>
 * v.enter(n)
 *   v.enterScope(n);
 *     c' = n.enterScope(c)
 *   v' = copy(v) with c' for c
 * n' = n.visitChildren(v')
 * v.leave(n, n', v')
 *   v.addDecls(n, n')
 *     v.addDecls(n')
 *       n'.addDecls(c)
 * </pre>
 */
public class ContextVisitor extends ErrorHandlingVisitor {
    protected ContextVisitor outer;

    /**
     * Should MissingDependencyExceptions be rethrown? If not,
     * then the dependency is added to the scheduler, and
     * the visitor continues.
     */
    protected boolean rethrowMissingDependencies = false;

    /** The current context of this visitor. */
    protected Context context;

    public ContextVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
        this.outer = null;
        this.context = null;
    }

    public ContextVisitor rethrowMissingDependencies(boolean rethrow) {
        if (rethrow == this.rethrowMissingDependencies) {
            return this;
        }
        ContextVisitor cv = (ContextVisitor) this.copy();
        cv.rethrowMissingDependencies = rethrow;
        return cv;
    }

    @Override
    public NodeVisitor begin() {
        context = ts.createContext();
        outer = null;
        return super.begin();
    }

    /** Returns the context for this visitor.
     *
     *  @return Returns the context that is currently in use by this visitor.
     *  @see polyglot.types.Context
     */
    public Context context() {
        return context;
    }

    /** Returns a new ContextVisitor that is a copy of the current visitor,
     *  except with an updated context.
     *
     *  @param c The new context that is to be used.
     *  @return Returns a copy of this visitor with the new context
     *  {@code c}.
     */
    public ContextVisitor context(Context c) {
        ContextVisitor v = (ContextVisitor) this.copy();
        v.context = c;
        return v;
    }

    @Override
    public Node override(Node parent, Node n) {
        try {
            if (Report.should_report(Report.visit, 2))
                Report.report(2, ">> " + this + "::override " + n);

            Node m = lang().overrideContextVisit(n, parent, this);

            if (Report.should_report(Report.visit, 2))
                Report.report(2, "<< " + this + "::override " + n + " -> " + m);
            if (m == null) {
                return super.override(parent, n);
            } else {
                return m;
            }
        } catch (SemanticException e) {
            if (e.getMessage() != null) {
                Position position = e.position();

                if (position == null) {
                    position = n.position();
                }

                this.errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR, e.getMessage(), position);
            } else {
                // silent error; these should be thrown only
                // when the error has already been reported
            }
            return n;
        }
    }

    /**
     * Returns a new context based on the current context, the Node current
     * being visited ({@code parent}), and the Node that is being
     * entered ({@code n}).  This new context is to be used
     * for visiting {@code n}.
     *
     * @return The new context after entering Node {@code n}.
     */
    protected Context enterScope(Node parent, Node n) {
        if (parent != null) {
            return lang().enterChildScope(parent, n, context);
        }
        // no parent node yet.
        return lang().enterScope(n, context);
    }

    /**
     * Imperatively update the context with declarations to be added after
     * visiting the node. Subclasses can decide whether to use the old node
     * or the new node to add the declarations. The default is to use the new node.
     */
    protected void addDecls(Node old, Node n) {
        addDecls(n);
    }

    /**
     * Imperatively update the context with declarations to be added after
     * visiting the node.
     */
    protected void addDecls(Node n) {
        lang().addDecls(n, context);
    }

    @Override
    public final NodeVisitor enter(Node n) {
        throw new InternalCompilerError(
                "Cannot call enter(Node n) on a ContextVisitor; use enter(Node parent, Node n)"
                        + " instead");
    }

    @Override
    public final NodeVisitor enter(Node parent, Node n) {
        if (Report.should_report(Report.visit, 5)) Report.report(5, "enter(" + n + ")");

        if (prune) {
            return new PruningVisitor(lang());
        }

        try {
            ContextVisitor v = this;

            Context c = this.enterScope(parent, n);

            if (c != this.context) {
                v = (ContextVisitor) this.copy();
                v.context = c;
                v.outer = this;
                v.error = false;
            }

            return v.superEnter(parent, n);
        } catch (MissingDependencyException e) {
            if (Report.should_report(Report.frontend, 3)) e.printStackTrace();
            Scheduler scheduler = job.extensionInfo().scheduler();
            Goal g = scheduler.currentGoal();
            scheduler.addDependencyAndEnqueue(g, e.goal(), e.prerequisite());
            g.setUnreachableThisRun();

            // The context for visiting the children
            // isn't set up correctly, so prune the traversal here.
            // The context might also be incorrect for later siblings
            // of this node, so set a flag to prune until the scope
            // is popped.
            this.prune = true;
            if (this.rethrowMissingDependencies) {
                throw e;
            }
            return new PruningVisitor(lang());
        }
    }

    protected boolean prune;

    public NodeVisitor superEnter(Node parent, Node n) {
        return super.enter(parent, n);
    }

    @Override
    public final Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        // If the traversal was pruned, just return n since leaveCall
        // might expect a ContextVisitor, not a PruningVisitor.
        if (v instanceof PruningVisitor || prune) {
            return n;
        }

        try {
            Node m = super.leave(parent, old, n, v);
            this.addDecls(old, m);
            return m;
        } catch (MissingDependencyException e) {
            if (Report.should_report(Report.frontend, 3)) e.printStackTrace();
            Scheduler scheduler = job.extensionInfo().scheduler();
            Goal g = scheduler.currentGoal();
            scheduler.addDependencyAndEnqueue(g, e.goal(), e.prerequisite());
            g.setUnreachableThisRun();
            if (this.rethrowMissingDependencies) {
                throw e;
            }
        }
        return n;
    }
}

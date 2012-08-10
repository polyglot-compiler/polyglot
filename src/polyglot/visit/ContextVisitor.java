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

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.types.Context;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;

/**
 * A visitor which maintains a context throughout the visitor's pass.  This is 
 * the base class of the disambiguation and type checking visitors.
 *
 * TODO: update this documentation.
 * For a node <code>n</code> methods are called in this order:
 * <pre>
 * v.enter(n)
 *   v.enterScope(n);
 *     c' = n.enterScope(c)
 *   v' = copy(v) with c' for c
 * n' = n.visitChildren(v')
 * v.leave(n, n', v')
 *   v.addDecls(n')
 *     n.addDecls(c)
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
     *  <code>c</code>.
     */
    public ContextVisitor context(Context c) {
        ContextVisitor v = (ContextVisitor) this.copy();
        v.context = c;
        return v;
    }

    /**
     * Returns a new context based on the current context, the Node current 
     * being visited (<code>parent</code>), and the Node that is being 
     * entered (<code>n</code>).  This new context is to be used
     * for visiting <code>n</code>. 
     *
     * @return The new context after entering Node <code>n</code>.
     */
    protected Context enterScope(Node parent, Node n) {
        if (parent != null) {
            return parent.del().enterChildScope(n, context);
        }
        // no parent node yet.
        return n.del().enterScope(context);
    }

    /**
     * Imperatively update the context with declarations to be added after
     * visiting the node.
     */
    protected void addDecls(Node n) {
        n.del().addDecls(context);
    }

    @Override
    public final NodeVisitor enter(Node n) {
        throw new InternalCompilerError("Cannot call enter(Node n) on a ContextVisitor; use enter(Node parent, Node n) instead");
    }

    @Override
    public final NodeVisitor enter(Node parent, Node n) {
        if (Report.should_report(Report.visit, 5))
            Report.report(5, "enter(" + n + ")");

        if (prune) {
            return new PruningVisitor();
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
        }
        catch (MissingDependencyException e) {
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
            return new PruningVisitor();
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
            this.addDecls(m);
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
        }
        return n;
    }
}

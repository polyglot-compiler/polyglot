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

import polyglot.ast.ClassMember;
import polyglot.ast.Import;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.SourceFile;
import polyglot.ast.Stmt;
import polyglot.frontend.Job;
import polyglot.main.Report;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.Position;

/**
 */
public class ErrorHandlingVisitor extends HaltingVisitor {
    protected boolean error;
    protected Job job;
    protected TypeSystem ts;
    protected NodeFactory nf;

    public ErrorHandlingVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(nf.lang());
        this.job = job;
        this.ts = ts;
        this.nf = nf;
    }

    /** Returns the {@code Job} that this Visitor is part of.
     *
     * @see polyglot.frontend.Job
     */
    public Job job() {
        return job;
    }

    /**
     * Part of the initialization done by begin() in an ErrorHandlingVisitor
     * method is initializing the error-handling state.
     */
    @Override
    public NodeVisitor begin() {
        this.error = false;
        return super.begin();
    }

    /** Returns the {@code ErrorQueue} for the current Job.
     *
     * @see polyglot.util.ErrorQueue
     */
    public ErrorQueue errorQueue() {
        return job().compiler().errorQueue();
    }

    /**
     * Returns true if some errors have been reported, even if cleared.
     */
    public boolean hasErrors() {
        return errorQueue().hasErrors();
    }

    /** Returns the {@code NodeFactory} that this Visitor is using.
     *
     * @see polyglot.ast.NodeFactory
     */
    public NodeFactory nodeFactory() {
        return nf;
    }

    /** Returns the {@code TypeSystem} that this Visitor is using.
     *
     * @see polyglot.types.TypeSystem
     */
    public TypeSystem typeSystem() {
        return ts;
    }

    /** Replaces the functionality of the {@code enter()} method; all
     * sub-classes should over-ride this method instead of
     * {@code enter()} if there is any chance of exceptions being
     * generated.
     *
     * This method is the replacement for the {@code enter()} method,
     * so that all of its subclasses gain the error handling capabilities
     * of this visitor without having to rewrite it for the
     * {@code enter()} for each sub-class.
     *
     * This method allows for a {@code SemanticException} to be
     * thrown in the body, while {@code enter()} does not.
     *
     * @see polyglot.visit.NodeVisitor#enter(Node, Node)
     * @throws SemanticException
     * @param n The root of the subtree to be traversed.
     * @return The {@code ErrorHandlingVisitor} which should be
     * used to visit the children of {@code n}.
     */
    protected NodeVisitor enterCall(Node parent, Node n) throws SemanticException {
        if (Report.should_report(Report.visit, 3))
            Report.report(3, "enter: " + parent + " -> " + n);
        return enterCall(n);
    }

    /**
     * @throws SemanticException
     */
    protected NodeVisitor enterCall(Node n) throws SemanticException {
        return this;
    }

    /** This method determines what should be returned by {@code enter()}
     * should its call to {@code enterCall()} throw a
     * {@code SemanticException}.
     *
     * @param n The root of the subtree that was traversed.
     * @return The {@code ErrorHandlingVisitor} which should be
     * used to visit the children of {@code n}.
     */
    protected NodeVisitor enterError(Node n) {
        return this;
    }

    /** Contains all of the functionality that can be done in the {@code leave}
     *  method, but allows {@code SemanticExceptions} to be
     * thrown.
     *
     * This method is in addition to the {@code leave} method,
     * and allows the compiler writer to write code that can throw errors
     * and let the Polyglot infrastructure handle the exceptions.
     *
     * @see polyglot.visit.NodeVisitor#leave(Node, Node, NodeVisitor)
     * @throws SemanticException
     * @param old The original state of root of the current subtree.
     * @param n The current state of the root of the current subtree.
     * @param v The {@code NodeVisitor} object used to visit the children.
     * @return The final result of the traversal of the tree rooted at
     * {@code n}.
     */
    protected Node leaveCall(Node parent, Node old, Node n, NodeVisitor v)
            throws SemanticException {

        return leaveCall(old, n, v);
    }

    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {

        return leaveCall(n);
    }

    /**
     * @throws SemanticException
     */
    protected Node leaveCall(Node n) throws SemanticException {
        return n;
    }

    /** Return true if we should catch errors thrown when visiting the node. */
    protected boolean catchErrors(Node n) {
        return n instanceof Stmt
                || n instanceof ClassMember
                || n instanceof Import
                || n instanceof SourceFile;
    }

    /**
     * Begin normal traversal of a subtree rooted at {@code n}. This gives
     * the visitor the option of changing internal state or returning a new
     * visitor which will be used to visit the children of {@code n}.
     *
     * This method delegates all responsibility of functionality to the
     * {@code enterCall} method, and handles and reports any exceptions
     * generated by {@code enterCall}.
     *
     * In overriding this method, unless the class explicitly does not
     * want to maintain any of the error handling aspects of this class, a call
     * {@code super.enter} should be embedded within the method at the
     * end.
     *
     * @param n The root of the subtree to be traversed.
     * @return The {@code NodeVisitor} which should be used to visit the
     * children of {@code n}.
     */
    @Override
    public NodeVisitor enter(Node parent, Node n) {
        if (Report.should_report(Report.visit, 5)) Report.report(5, "enter(" + n + ")");

        if (catchErrors(n)) {
            this.error = false;
        }

        try {
            // should copy the visitor
            return enterCall(parent, n);
        } catch (SemanticException e) {
            if (e.getMessage() != null) {
                Position position = e.position();

                if (position == null) {
                    position = n.position();
                }

                errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR, e.getMessage(), position);
            } else {
                // silent error; these should be thrown only
                // when the error has already been reported
            }

            if (!catchErrors(n)) {
                this.error = true;
            }

            return enterError(n);
        }
    }

    /**
     * This method is called after all of the children of {@code n}
     * have been visited. In this case, these children were visited by the
     * visitor {@code v}. This is the last chance for the visitor to
     * modify the tree rooted at {@code n}. This method will be called
     * exactly the same number of times as {@code entry} is called.
     * That is, for each node that is not overridden, {@code enter} and
     * {@code leave} are each called exactly once.
     * <p>
     * Note that if {@code old == n} then the visitor should make a copy
     * of {@code n} before modifying it. It should then return the
     * modified copy.
     *
     * This method delegates all responsibility of functionality to the
     * {@code leaveCall} method, and handles and reports any exceptions
     * generated by {@code leaveCall}.
     *
     * In overriding this method, unless the class explicitly does not
     * want to maintain any of the error handling aspects of this class, a call
     * {@code super.leave} should be embedded within the method at the
     * end.
     *
     * @param old The original state of root of the current subtree.
     * @param n The current state of the root of the current subtree.
     * @param v The {@code NodeVisitor} object used to visit the children.
     * @return The final result of the traversal of the tree rooted at
     * {@code n}.
     */
    @Override
    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        try {
            if (v instanceof ErrorHandlingVisitor && ((ErrorHandlingVisitor) v).error) {

                if (Report.should_report(Report.visit, 5))
                    Report.report(5, "leave(" + n + "): error below");

                if (catchErrors(n)) {
                    this.error = false;
                    ((ErrorHandlingVisitor) v).error = false;
                } else {
                    // propagate the error outward
                    this.error = true;
                }

                // don't visit the node
                return n;
            }

            if (Report.should_report(Report.visit, 5))
                Report.report(5, "leave(" + n + "): calling leaveCall");

            return leaveCall(parent, old, n, v);
        } catch (SemanticException e) {
            if (e.getMessage() != null) {
                Position position = e.position();

                if (position == null) {
                    position = n.position();
                }

                errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR, e.getMessage(), position);
            } else {
                // silent error; these should be thrown only
                // when the error has already been reported
            }

            if (catchErrors(n)) {
                this.error = false;
                ((ErrorHandlingVisitor) v).error = false;
            } else {
                this.error = true;
                ((ErrorHandlingVisitor) v).error = true;
            }

            return n;
        }
    }
}

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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SubtypeSet;

/** Visitor which checks if exceptions are caught or declared properly. */
public class ExceptionChecker extends ErrorHandlingVisitor {
    protected ExceptionChecker outer;

    /**
     * Set of exceptions that can be caught. Combined with the outer
     * field, these sets form a stack of exceptions, representing
     * all and only the exceptions that may be thrown at this point in
     * the code.
     * 
     * Note: Consider the following code, where A,B,C,D are Exception subclasses.
     *    void m() throws A, B {
     *       try {
     *          ...
     *       }
     *       catch (C ex) { ... }
     *       catch (D ex) { ... }
     *    }
     *    
     *  Inside the try-block, the stack of catchable sets is:
     *     { C }
     *     { D }
     *     { A, B }
     */
    protected Set<Type> catchable;

    /**
     * The throws set, calculated bottom up.
     */
    protected SubtypeSet throwsSet;

    /**
     * Responsible for creating an appropriate exception.
     */
    protected UncaughtReporter reporter;

    /**
     * Should the propogation of eceptions upwards go past this point?
     */
    protected boolean catchAllThrowable;

    public ExceptionChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
        this.outer = null;
        this.catchAllThrowable = false;
    }

    public ExceptionChecker push(UncaughtReporter reporter) {
        ExceptionChecker ec = this.push();
        ec.reporter = reporter;
        ec.throwsSet = new SubtypeSet(ts.Throwable());
        return ec;
    }

    public ExceptionChecker push(Type catchableType) {
        ExceptionChecker ec = this.push();
        ec.catchable = Collections.singleton(catchableType);
        ec.throwsSet = new SubtypeSet(ts.Throwable());
        return ec;
    }

    public ExceptionChecker push(Collection<? extends Type> catchableTypes) {
        ExceptionChecker ec = this.push();
        ec.catchable = new HashSet<Type>(catchableTypes);
        ec.throwsSet = new SubtypeSet(ts.Throwable());
        return ec;
    }

    public ExceptionChecker pushCatchAllThrowable() {
        ExceptionChecker ec = this.push();
        ec.throwsSet = new SubtypeSet(ts.Throwable());
        ec.catchAllThrowable = true;
        return ec;
    }

    public ExceptionChecker push() {
        throwsSet(); // force an instantiation of the throwsset.
        ExceptionChecker ec = (ExceptionChecker) this.visitChildren();
        ec.outer = this;
        ec.catchable = null;
        ec.catchAllThrowable = false;
        return ec;
    }

    public ExceptionChecker pop() {
        return outer;
    }

    @Override
    protected NodeVisitor enterCall(Node n) throws SemanticException {
        return n.del().exceptionCheckEnter(this);
    }

    @Override
    protected NodeVisitor enterError(Node n) {
        return push();
    }

    /**
     * Call exceptionCheck(ExceptionChecker) on the node.
     *
     * @param old The original state of root of the current subtree.
     * @param n The current state of the root of the current subtree.
     * @param v The <code>NodeVisitor</code> object used to visit the children.
     * @return The final result of the traversal of the tree rooted at 
     *  <code>n</code>.
     */
    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {

        ExceptionChecker inner = (ExceptionChecker) v;

        {
            // code in this block checks the invariant that
            // this ExceptionChecker must be an ancestor of inner, i.e.,
            // inner must be the result of zero or more pushes.
            boolean isAncestor = false;
            ExceptionChecker ec = inner;
            while (!isAncestor && ec != null) {
                isAncestor = isAncestor || (ec == this);
                ec = ec.outer;
            }
            if (!isAncestor) {
                throw new InternalCompilerError("oops!");
            }
        }

        // gather exceptions from this node.
        return n.del().exceptionCheck(inner);
    }

    /**
     * The ast nodes will use this callback to notify us that they throw an
     * exception of type t. This method will throw a SemanticException if the
     * type t is not allowed to be thrown at this point; the exception t will be
     * added to the throwsSet of all exception checkers in the stack, up to (and
     * not including) the exception checker that catches the exception.
     * 
     * @param t The type of exception that the node throws.
     * @throws SemanticException
     */
    public void throwsException(Type t, Position pos) throws SemanticException {
        if (!t.isUncheckedException()) {
            // go through the stack of catches and see if the exception
            // is caught.
            boolean exceptionCaught = false;
            ExceptionChecker ec = this;
            while (!exceptionCaught && ec != null) {
                if (ec.catchable != null) {
                    for (Type catchType : ec.catchable) {
                        if (ts.isSubtype(t, catchType)) {
                            exceptionCaught = true;
                            break;
                        }
                    }
                }
                if (!exceptionCaught && ec.throwsSet != null) {
                    // add t to ec's throwsSet.
                    ec.throwsSet.add(t);
                }
                if (ec.catchAllThrowable) {
                    // stop the propagation
                    exceptionCaught = true;
                }
                ec = ec.pop();
            }
            if (!exceptionCaught) {
                reportUncaughtException(t, pos);
            }
        }
    }

    public SubtypeSet throwsSet() {
        if (this.throwsSet == null) {
            this.throwsSet = new SubtypeSet(ts.Throwable());
        }
        return this.throwsSet;
    }

    protected void reportUncaughtException(Type t, Position pos)
            throws SemanticException {
        ExceptionChecker ec = this;
        UncaughtReporter ur = null;
        while (ec != null && ur == null) {
            ur = ec.reporter;
            ec = ec.outer;
        }
        if (ur == null) {
            ur = new UncaughtReporter();
        }
        ur.uncaughtType(t, pos);
    }

    public static class UncaughtReporter {
        /**
         * This method must throw a SemanticException, reporting
         * that the Exception type t must be caught.
         * @throws SemanticException 
         */
        void uncaughtType(Type t, Position pos) throws SemanticException {
            throw new SemanticException("The exception \"" + t
                    + "\" must either be caught or declared to be thrown.", pos);
        }
    }

    public static class CodeTypeReporter extends UncaughtReporter {
        public final String codeType;

        public CodeTypeReporter(String codeType) {
            this.codeType = codeType;
        }

        @Override
        void uncaughtType(Type t, Position pos) throws SemanticException {
            throw new SemanticException("A " + codeType + " can not "
                    + "throw a \"" + t + "\".", pos);
        }
    }

}

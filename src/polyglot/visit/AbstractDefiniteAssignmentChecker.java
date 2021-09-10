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

import java.util.Map.Entry;
import java.util.Set;

import polyglot.ast.ClassBody;
import polyglot.ast.CodeNode;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Field;
import polyglot.ast.FieldAssign;
import polyglot.ast.FieldDecl;
import polyglot.ast.Initializer;
import polyglot.ast.Local;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

/**
 * Visitor which checks that all local variables must be defined before use,
 * and that final variables and fields are initialized correctly.
 *
 * The checking of the rules is implemented in the methods leaveCall(Node)
 * and check(FlowGraph, Term, Item, Item).
 *
 * If language extensions have new constructs that use local variables, they can
 * override the method {@code checkOther} to check that the uses of these
 * local variables are correctly initialized. (The implementation of the method will
 * probably call checkLocalInstanceInit to see if the local used is initialized).
 *
 * If language extensions have new constructs that assign to local variables,
 * they can override the method {@code flowOther} to capture the way
 * the new construct's initialization behavior.
 *
 * This class provides functionality for doing the assignment checking, but
 * leaves abstract the ClassBodyInfo and FlowItem implementations.
 */
public abstract class AbstractDefiniteAssignmentChecker<
                CBI extends AbstractAssignmentChecker.ClassBodyInfo<CBI>,
                FI extends AbstractAssignmentChecker.FlowItem>
        extends AbstractAssignmentChecker<CBI, FI> {
    public AbstractDefiniteAssignmentChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected void leaveClassBody(ClassBody n) throws SemanticException {
        // Check that all static fields have been initialized at least once.
        checkStaticFinalFieldsInit(n);

        // Check that at the end of each constructor, all non-static final
        // fields are initialized.
        checkNonStaticFinalFieldsInit(n);
    }

    /**
     * Check that each static final field is initialized at least once.
     *
     * @param cb The ClassBody of the class declaring the fields to check.
     * @throws SemanticException
     */
    protected void checkStaticFinalFieldsInit(ClassBody cb) throws SemanticException {
        // Checks that all static final fields have been initialized at least
        // once.
        for (Entry<FieldInstance, AssignmentStatus> e :
                curCBI.curClassFieldAsgtStatuses.entrySet()) {
            FieldInstance fi = e.getKey();
            if (fi.flags().isStatic() && fi.flags().isFinal()) {
                AssignmentStatus defAss = e.getValue();
                if (!defAss.definitelyAssigned) {
                    throw new SemanticException(
                            "Final field \"" + fi.name() + "\" might not have been initialized",
                            fi.position());
                }
            }
        }
    }

    /**
     * Check that each non static final field has been initialized exactly once,
     * taking into account the fact that constructors may call other
     * constructors.
     *
     * @param cb The ClassBody of the class declaring the fields to check.
     * @throws SemanticException
     */
    protected void checkNonStaticFinalFieldsInit(ClassBody cb) throws SemanticException {
        // For each non-static final field instance, check that all constructors
        // initialize it exactly once, taking into account constructor calls.
        for (FieldInstance fi : curCBI.curClassFieldAsgtStatuses.keySet()) {
            if (fi.flags().isFinal() && !fi.flags().isStatic()) {
                // The field is final and not static, so it must be initialized
                // exactly once.
                // navigate up through all of the the constructors
                // that this constructor calls.

                AssignmentStatus ic = curCBI.curClassFieldAsgtStatuses.get(fi.orig());
                boolean fieldInitializedBeforeConstructors = ic != null && ic.definitelyAssigned;

                for (ConstructorDecl cd : curCBI.allConstructors) {
                    boolean isInitialized = fieldInitializedBeforeConstructors;

                    ConstructorInstance ci = cd.constructorInstance();
                    Set<FieldInstance> s = curCBI.fieldsConstructorInitializes.get(ci.orig());
                    if (s != null && s.contains(fi)) {
                        if (isInitialized) {
                            throw new SemanticException(
                                    "Final field \""
                                            + fi.name()
                                            + "\" might have already been initialized",
                                    cd.position());
                        }

                        isInitialized = true;
                    }

                    if (!isInitialized) {
                        // check whether this constructor can terminate normally.
                        if (!curCBI.constructorsCannotTerminateNormally.contains(cd)) {
                            throw new SemanticException(
                                    "Final field \""
                                            + fi.name()
                                            + "\" might not have been initialized",
                                    cd.position().endOf());
                        } else {
                            // Even though the final field may not be
                            // initialized, the constructor cannot terminate
                            // normally. For compatibility with javac, we will
                            // not protest.
                        }
                    }
                }
            }
        }
    }

    /**
     * Determines whether the current {@link CodeNode} being processed by the
     * dataflow equations is part of a class's initialization code. For the base
     * language, this is exactly when the {@link CodeNode} is a {@link
     * FieldDecl}, a {@link ConstructorDecl}, or a {@link Initializer}.
     */
    protected boolean duringObjectInit() {
        return curCBI.curCodeDecl instanceof FieldDecl
                || curCBI.curCodeDecl instanceof ConstructorDecl
                || curCBI.curCodeDecl instanceof Initializer;
    }

    /**
     * {@inheritDoc}
     * See JLS 2nd Ed. | 16: Every blank final field must have a definitely
     * assigned value when any access of its value occurs.
     */
    @Override
    protected void checkField(FlowGraph<FI> graph, Field f, FI dfIn) throws SemanticException {
        FieldInstance fi = f.fieldInstance();
        // Use of blank final field only needs to be checked when the use
        // occurs inside the same class as the field's container.
        if (fi.flags().isFinal() && ts.typeEquals(curCBI.curClass, fi.container())) {
            if (duringObjectInit() && isFieldsTargetAppropriate(graph, f)) {
                AssignmentStatus initCount = dfIn.assignmentStatus.get(fi.orig());
                if (initCount == null || !initCount.definitelyAssigned) {
                    throw new SemanticException(
                            "Final field \"" + f.name() + "\" might not have been initialized",
                            f.position());
                }
            }
        }
    }

    @Override
    protected void checkLocal(FlowGraph<FI> graph, Local l, FI dfIn) throws SemanticException {
        if (!curCBI.localDeclarations.contains(l.localInstance().orig())) {
            // it's a local variable that has not been declared within
            // this scope. The only way this can arise is from an
            // inner class that is not a member of a class (typically
            // a local class, or an anonymous class declared in a method,
            // constructor or initializer).
            // We need to check that it is a final local, and also
            // keep track of it, to ensure that it has been definitely
            // assigned at this point.
            curCBI.outerLocalsUsed.add(l.localInstance().orig());
        } else {
            AssignmentStatus initCount = dfIn.assignmentStatus.get(l.localInstance().orig());
            if (initCount == null || !initCount.definitelyAssigned) {
                // the local variable may not have been initialized.
                // However, we only want to complain if the local is reachable
                if (l.reachable()) {
                    throw new SemanticException(
                            "Local variable \"" + l.name() + "\" may not have been initialized",
                            l.position());
                }
            }
        }
    }

    protected void checkLocalInstanceInit(LocalInstance li, FlowItem dfIn, Position pos)
            throws SemanticException {
        AssignmentStatus initCount = dfIn.assignmentStatus.get(li.orig());
        if (initCount != null && !initCount.definitelyAssigned) {
            // the local variable may not have been initialized.
            throw new SemanticException(
                    "Local variable \"" + li.name() + "\" may not have been initialized", pos);
        }
    }

    @Override
    protected void checkLocalAssign(FlowGraph<FI> graph, LocalInstance li, Position pos, FI dfIn)
            throws SemanticException {
        if (!curCBI.localDeclarations.contains(li.orig())) {
            throw new SemanticException(
                    "Final local variable \""
                            + li.name()
                            + "\" cannot be assigned to in an inner class.",
                    pos);
        }

        AssignmentStatus initCount = dfIn.assignmentStatus.get(li.orig());

        if (li.flags().isFinal() && initCount != null && !initCount.definitelyUnassigned) {
            throw new SemanticException(
                    "Final variable \"" + li.name() + "\" might already have been initialized",
                    pos);
        }
    }

    @Override
    protected void checkFieldAssign(FlowGraph<FI> graph, FieldAssign a, FI dfIn)
            throws SemanticException {

        Field f = a.left();
        FieldInstance fi = f.fieldInstance();
        if (fi.flags().isFinal()) {
            if (duringObjectInit()
                    && isFieldsTargetAppropriate(graph, f)
                    && ts.equals(curCBI.curClass, fi.container())) {
                // we are in a constructor or initializer block and
                // if the field is static then the target is the class
                // at hand, and if it is not static then the
                // target of the field is this.
                // So a final field in this situation can be
                // assigned to at most once.
                AssignmentStatus initCount = dfIn.assignmentStatus.get(fi.orig());
                if (initCount == null) {
                    // This should not happen.
                    throw new InternalCompilerError(
                            "Dataflow information not found for field \"" + fi.name() + "\".",
                            a.position());
                }
                if (!initCount.definitelyUnassigned) {
                    throw new SemanticException(
                            "Final field \"" + fi.name() + "\" might already have been initialized",
                            a.position());
                }
            } else {
                // not in a constructor or initializer, or the target is
                // not appropriate. So we cannot assign
                // to a final field at all.
                throw new SemanticException(
                        "Cannot assign a value "
                                + "to final field \""
                                + fi.name()
                                + "\" of \""
                                + fi.orig().container()
                                + "\".",
                        a.position());
            }
        }
    }

    @Override
    protected void checkLocalsUsedByInnerClass(
            FlowGraph<FI> graph, ClassBody cb, Set<LocalInstance> localsUsed, FI dfIn, FI dfOut)
            throws SemanticException {
        for (LocalInstance li : localsUsed) {
            AssignmentStatus initCount = dfOut.assignmentStatus.get(li.orig());
            if (!curCBI.localDeclarations.contains(li.orig())) {
                // the local wasn't defined in this scope.
                curCBI.outerLocalsUsed.add(li.orig());
            } else if (initCount == null || !initCount.definitelyAssigned) {
                // initCount will in general not be null, as the local variable
                // li is declared in the current class; however, if the inner
                // class is declared in the initializer of the local variable
                // declaration, then initCount could in fact be null, as we
                // leave the inner class before we have performed flowLocalDecl
                // for the local variable declaration.

                throw new SemanticException(
                        "Local variable \""
                                + li.name()
                                + "\" must be initialized before the class "
                                + "declaration.",
                        cb.position());
            }
        }
    }
}

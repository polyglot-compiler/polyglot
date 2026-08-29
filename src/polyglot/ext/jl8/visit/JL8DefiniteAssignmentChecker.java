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
package polyglot.ext.jl8.visit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Unary;
import polyglot.ext.jl5.visit.JL5DefiniteAssignmentChecker;
import polyglot.ext.jl8.ast.FunctionSpec;
import polyglot.ext.jl8.ast.FunctionValue;
import polyglot.ext.jl8.ast.JL8UnaryExt;
import polyglot.ext.jl8.ast.LambdaExpression;
import polyglot.ext.jl8.types.JL8LocalInstance;
import polyglot.frontend.Job;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.FlowGraph;
import polyglot.visit.NodeVisitor;

/**
 * Extends definite assignment checking to lambda expressions, and checks the
 * Java 8 rule that a local variable of an enclosing method referred to from a
 * lambda expression or a nested class body must be effectively final
 * (JLS 4.12.4).
 * <p>
 * A variable is effectively final if it is never assigned at a point where it
 * might already have a value. That is exactly the condition the base compiler
 * enforces for a variable declared final, so this checker applies that condition
 * to captured variables too. Type checking has recorded which variables are
 * captured; see JL8LocalExt.
 */
public class JL8DefiniteAssignmentChecker extends JL5DefiniteAssignmentChecker {

    /**
     * The locals of an enclosing method that each lambda expression refers to.
     * This plays the part that {@code ClassBodyInfo.localsUsedInClassBodies}
     * plays for class bodies, which cannot be reused because it is keyed by
     * {@code ClassBody}.
     */
    protected final Map<LambdaExpression, Set<LocalInstance>> localsUsedInLambdas =
            new HashMap<>();

    public JL8DefiniteAssignmentChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    /**
     * The body of a lambda expression is a code scope nested inside the method
     * that contains it, just as the body of an anonymous class is, so it needs
     * its own {@code ClassBodyInfo}. Without one, the locals of the enclosing
     * method would look to the analysis as though they were declared in the
     * lambda body itself.
     * <p>
     * The lambda is not a class of its own, so the new {@code ClassBodyInfo} is
     * for the same class, and starts with the assignment statuses the enclosing
     * scope had reached, so that the fields of that class are analyzed as they
     * would have been had the lambda body been written in place.
     */
    @Override
    protected NodeVisitor enterCall(Node parent, Node n) throws SemanticException {
        if (n instanceof LambdaExpression) {
            ClassBodyInfo outer = curCBI;
            curCBI = newCBI(outer, outer.curClass);
            curCBI.curClassFieldAsgtStatuses.putAll(outer.curClassFieldAsgtStatuses);
        }
        return super.enterCall(parent, n);
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (n instanceof LambdaExpression) {
            try {
                // The superclass performs the dataflow over the lambda body,
                // which is what records the locals it refers to.
                Node result = super.leaveCall(old, n, v);
                localsUsedInLambdas.put((LambdaExpression) n, curCBI.outerLocalsUsed);
                return result;
            } finally {
                curCBI = curCBI.outer;
            }
        }
        return super.leaveCall(old, n, v);
    }

    @Override
    protected void checkLocalAssign(
            FlowGraph<FlowItem> graph, LocalInstance li, Position pos, FlowItem dfIn)
            throws SemanticException {
        super.checkLocalAssign(graph, li, pos, dfIn);

        Position capturedAt = capturedAt(li);
        if (capturedAt == null) return;

        AssignmentStatus asgtStatus = dfIn.assignmentStatus.get(li.orig());
        if (asgtStatus != null && !asgtStatus.definitelyUnassigned) {
            throw new SemanticException(
                    "Local variable \""
                            + li.name()
                            + "\" is referred to from a class body or lambda expression on line "
                            + capturedAt.line()
                            + ", so it must be effectively final, but it might already have been"
                            + " assigned here.",
                    pos);
        }
    }

    @Override
    protected void checkOther(FlowGraph<FlowItem> graph, Node n, FlowItem dfIn, FlowItem dfOut)
            throws SemanticException {
        if (n instanceof FunctionValue) {
            // In the enclosing flow graph the lambda expression is wrapped in a
            // FunctionValue; the LambdaExpression itself is the root of its own
            // graph, and is not a term of this one.
            FunctionSpec spec = ((FunctionValue) n).functionSpec();
            if (spec instanceof LambdaExpression) {
                checkLocalsUsedByLambda(graph, (LambdaExpression) spec, n.position(), dfOut);
            }
        } else if (n instanceof Unary) {
            checkUnary(graph, (Unary) n, dfIn);
        }
        super.checkOther(graph, n, dfIn, dfOut);
    }

    /**
     * Checks that the locals of this method that {@code lambda} refers to have
     * been assigned by the time the lambda expression is evaluated. This is the
     * counterpart, for lambda expressions, of
     * {@code checkLocalsUsedByInnerClass}.
     */
    protected void checkLocalsUsedByLambda(
            FlowGraph<FlowItem> graph, LambdaExpression lambda, Position pos, FlowItem dfOut)
            throws SemanticException {
        Set<LocalInstance> localsUsed = localsUsedInLambdas.get(lambda);
        if (localsUsed == null) return;

        for (LocalInstance li : localsUsed) {
            if (!curCBI.localDeclarations.contains(li.orig())) {
                // The local is declared outside this scope too, so it is the
                // enclosing scope that must account for it.
                curCBI.outerLocalsUsed.add(li.orig());
                continue;
            }
            AssignmentStatus asgtStatus = dfOut.assignmentStatus.get(li.orig());
            if (asgtStatus == null || !asgtStatus.definitelyAssigned) {
                // Report at the reference inside the lambda body if we know
                // where it is: a lambda expression is rewritten before this pass
                // runs, so the term in this flow graph has no source position.
                Position capturedAt = capturedAt(li);
                throw new SemanticException(
                        "Local variable \"" + li.name() + "\" may not have been initialized",
                        capturedAt == null ? pos : capturedAt);
            }
        }
    }

    /**
     * Incrementing or decrementing a variable assigns to it, but the base
     * compiler rejects ++ and -- on a final variable while type checking, so it
     * has no reason to treat them as assignments here. A captured variable is
     * not rejected while type checking, because it is not yet known to be
     * captured, so the increment must be checked now.
     */
    protected void checkUnary(FlowGraph<FlowItem> graph, Unary u, FlowItem dfIn)
            throws SemanticException {
        if (!JL8UnaryExt.isIncrementOrDecrement(u.operator())) return;
        if (!(u.expr() instanceof Local)) return;

        LocalInstance li = ((Local) u.expr()).localInstance();
        if (li != null && capturedAt(li) != null) {
            checkLocalAssign(graph, li, u.position(), dfIn);
        }
    }

    /**
     * The position at which {@code li} is referred to from a lambda expression
     * or a nested class body, or null if it never is.
     */
    protected Position capturedAt(LocalInstance li) {
        LocalInstance orig = li.orig();
        if (orig instanceof JL8LocalInstance) return ((JL8LocalInstance) orig).capturedAt();
        return null;
    }
}

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

package polyglot.ast;

import java.util.List;

import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ConstantChecker;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * {@code JLang} contains methods implemented by an AST node that are specific
 * to the Java language.
 */
public interface JLang extends Lang {
    /**
     * Disambiguate the AST.
     *
     * This method is called by the {@code override()} method of the
     * visitor.  If this method returns non-null, the node's children
     * will not be visited automatically.  Thus, the method should check
     * both the node {@code this} and it's children, usually by
     * invoking {@code visitChildren} with {@code tc} or
     * with another visitor, returning a non-null node.  OR, the method
     * should do nothing and simply return {@code null} to allow
     * {@code enter}, {@code visitChildren}, and {@code leave}
     * to be invoked on the node.
     *
     * The default implementation returns {@code null}.
     * Overriding of this method is discouraged, but sometimes necessary.
     *
     * @param ar The visitor which disambiguates.
     */
    Node disambiguateOverride(Node n, Node parent, AmbiguityRemover ar)
            throws SemanticException;

    /**
     * Remove any remaining ambiguities from the AST.
     *
     * This method is called by the {@code enter()} method of the
     * visitor.  The * method should perform work that should be done
     * before visiting the children of the node.  The method may return
     * {@code this} or a new copy of the node on which
     * {@code visitChildren()} and {@code leave()} will be
     * invoked.
     *
     * @param ar The visitor which disambiguates.
     */
    NodeVisitor disambiguateEnter(Node n, AmbiguityRemover ar)
            throws SemanticException;

    /**
     * Remove any remaining ambiguities from the AST.
     *
     * This method is called by the {@code leave()} method of the
     * visitor.  The method should perform work that should be done
     * after visiting the children of the node.  The method may return
     * {@code this} or a new copy of the node which will be
     * installed as a child of the node's parent.
     *
     * The node should not assume that its children have been disambiguated.
     * If it depends on a child being disambiguated,
     * it may just return {@code this} without doing any work.
     *
     * @param ar The visitor which disambiguates.
     */
    Node disambiguate(Node n, AmbiguityRemover ar) throws SemanticException;

    /**
     * Get the expected type of a child expression of {@code this}.
     * The expected type is determined by the context in that the child occurs
     * (e.g., for {@code x = e}, the expected type of {@code e} is
     * the declared type of {@code x}.
     *
     * The expected type should impose the least constraints on the child's
     * type that are allowed by the parent node.
     *
     * @param child A child expression of this node.
     * @param av An ascription visitor.
     * @return The expected type of {@code child}.
     */
    Type childExpectedType(Node n, Expr child, AscriptionVisitor av);

    /**
     * Check if the node is a compile-time constant.
     *
     * This method is called by the {@code leave()} method of the
     * visitor.  The method should perform work that should be done
     * after visiting the children of the node.  The method may return
     * {@code this} or a new copy of the node which will be
     * installed as a child of the node's parent.
     *
     * @param cc The constant checking visitor.
     */
    Node checkConstants(Node n, ConstantChecker cc) throws SemanticException;

    /**
     * Check that exceptions are properly propagated throughout the AST.
     *
     * This method is called by the {@code enter()} method of the
     * visitor.  The * method should perform work that should be done
     * before visiting the children of the node.  The method may return
     * {@code this} or a new copy of the node on which
     * {@code visitChildren()} and {@code leave()} will be
     * invoked.
     *
     * @param ec The visitor.
     */
    NodeVisitor exceptionCheckEnter(Node n, ExceptionChecker ec)
            throws SemanticException;

    /**
     * Check that exceptions are properly propagated throughout the AST.
     *
     * This method is called by the {@code leave()} method of the
     * visitor.  The method should perform work that should be done
     * after visiting the children of the node.  The method may return
     * {@code this} or a new copy of the node which will be
     * installed as a child of the node's parent.
     *
     * @param ec The visitor.
     */
    Node exceptionCheck(Node n, ExceptionChecker ec) throws SemanticException;

    /** 
     * List of Types of exceptions that might get thrown.  The result is
     * not necessarily correct until after type checking. 
     */
    List<Type> throwTypes(Node n, TypeSystem ts);

    // CallOps

    /**
     * Used to find the missing static target of a static method call.
     * Should return the container of the method instance. 
     * 
     */
    Type findContainer(Call n, TypeSystem ts, MethodInstance mi);

    ReferenceType findTargetType(Call n) throws SemanticException;

    /**
    * Typecheck the Call when the target is null. This method finds
    * an appropriate target, and then type checks accordingly.
    * 
    * @param argTypes list of {@code Type}s of the arguments
     * @throws SemanticException 
    */
    Node typeCheckNullTarget(Call n, TypeChecker tc, List<Type> argTypes)
            throws SemanticException;

    // ClassDeclOps

    void prettyPrintHeader(ClassDecl n, CodeWriter w, PrettyPrinter tr);

    void prettyPrintFooter(ClassDecl n, CodeWriter w, PrettyPrinter tr);

    Node addDefaultConstructor(ClassDecl n, TypeSystem ts, NodeFactory nf,
            ConstructorInstance defaultConstructorInstance)
            throws SemanticException;

    // LoopOps

    /** Returns true of cond() evaluates to a constant. */
    boolean condIsConstant(Loop n, JLang lang);

    /** Returns true if cond() is a constant that evaluates to true. */
    boolean condIsConstantTrue(Loop n, JLang lang);

    /** Returns true if cond() is a constant that evaluates to false. */
    boolean condIsConstantFalse(Loop n, JLang lang);

    /** Target of a continue statement in the loop body. */
    Term continueTarget(Loop n);

    // NewOps

    TypeNode findQualifiedTypeNode(New n, AmbiguityRemover ar, ClassType outer,
            TypeNode objectType) throws SemanticException;

    Expr findQualifier(New n, AmbiguityRemover ar, ClassType ct)
            throws SemanticException;

    void typeCheckFlags(New n, TypeChecker tc) throws SemanticException;

    void typeCheckNested(New n, TypeChecker tc) throws SemanticException;

    void printQualifier(New n, CodeWriter w, PrettyPrinter tr);

    void printShortObjectType(New n, CodeWriter w, PrettyPrinter tr);

    void printBody(New n, CodeWriter w, PrettyPrinter tr);

    ClassType findEnclosingClass(New n, Context c, ClassType ct);

    // ProcedureCallOps

    void printArgs(ProcedureCall n, CodeWriter w, PrettyPrinter tr);

    // ProcedureDeclOps

    void prettyPrintHeader(ProcedureDecl n, Flags flags, CodeWriter w,
            PrettyPrinter tr);

    // TermOps

    /**
     * Return the first direct subterm performed when evaluating this term. If
     * this term has no subterms, this should return null.
     * 
     * This method is similar to the deprecated entry(), but it should *not*
     * recursively drill down to the innermost subterm. The direct child visited
     * first in this term's dataflow should be returned.
     */
    Term firstChild(Term n);

    /**
     * Visit this term in evaluation order, calling v.edge() for each successor
     * in succs, if data flows on that edge.
     */
    <T> List<T> acceptCFG(Term n, CFGBuilder<?> v, List<T> succs);

    // TryOps

    /**
     * Construct an ExceptionChecker that is suitable for checking the try block of 
     * a try-catch-finally AST node. 
     * @param ec The exception checker immediately prior to the try block.
     * @return
     */
    ExceptionChecker constructTryBlockExceptionChecker(Try n,
            ExceptionChecker ec);

    /**
     * Perform exception checking of the try block of a try-catch-finally
     * AST node, using the supplied exception checker.
     * @param ec
     * @return
     * @throws SemanticException
     */
    Block exceptionCheckTryBlock(Try n, ExceptionChecker ec)
            throws SemanticException;

    /**
     * Perform exception checking of the catch blocks of a try-catch-finally
     * AST node, using the supplied exception checker.
     * 
     * @param ec
     * @return
     * @throws SemanticException
     */
    List<Catch> exceptionCheckCatchBlocks(Try n, ExceptionChecker ec)
            throws SemanticException;

    /**
     * Perform exception checking of the finally block of a try-catch-finally
     * AST node (if there is one), using the supplied exception checker.
     * 
     * @param ec
     * @return
     * @throws SemanticException
     */
    Block exceptionCheckFinallyBlock(Try n, ExceptionChecker ec)
            throws SemanticException;
}

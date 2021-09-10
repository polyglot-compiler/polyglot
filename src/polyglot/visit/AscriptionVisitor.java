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
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;

/** Visitor which allows type information to be utilized to perform AST
 * modifications.
 *
 * The major advantage of this visitor is the new {@code ascribe()}
 * method, which allows AST translations based on the expression
 * and also the type that is expected. For the base translation (standard
 * Java), the type of the expression and the type that is expected
 * are the same. Language extensions however may not have this property,
 * and can take advantage of the {@code ascribe} method to transform
 * the AST into a different form that will pass Java type-checking.
 *
 * @see #ascribe
 */
public class AscriptionVisitor extends ContextVisitor {
    protected Type type;
    protected AscriptionVisitor outer;

    /**
     *  Default constructor. See the constructor in {@code ErrorHandingVisitor}
     *  for more details.
     *
     *  @see polyglot.visit.ErrorHandlingVisitor#ErrorHandlingVisitor
     */
    public AscriptionVisitor(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
        type = null;
        outer = null;
    }

    @Override
    public JLang lang() {
        return (JLang) super.lang();
    }

    // FIXME: what does this do?
    /**
     */
    public AscriptionVisitor pop() {
        return outer;
    }

    /** Returns the type that is expected of the expression that is being
     *  visited.
     */
    public Type toType() {
        return type;
    }

    // TODO is this comment revealing too much implementation?
    /** Sets up the expected type information for later calls to
     *  {@code ascribe()}. Other than that, plays the same role
     *  as the {@code enterCall} method in
     *  {@code ErrorHandlingVisitor}.
     */
    @Override
    public NodeVisitor enterCall(Node parent, Node n) throws SemanticException {
        Type t = null;

        if (parent != null && n instanceof Expr) {
            t = lang().childExpectedType(parent, (Expr) n, this);
        }

        AscriptionVisitor v = (AscriptionVisitor) copy();
        v.outer = this;
        v.type = t;

        return v;
    }

    /** The {@code ascribe()} method is called for each expression
     * and is passed the type the expression is <i>used at</i> rather
     * than the type the type
     * checker assigns to it.
     *
     * For instance, with the following code:
     *
     *     {@code Object o = new Integer(3);}
     *
     * {@code ascribe()} will be called with expression
     * {@code new Integer(3)} and type {@code Object}.
     *
     * @param e The expression that is being visited
     * @param toType The type that the parent node is expecting.
     * @return The new translated Expr node, or if nothing has changed, just
     * e.
     * @throws SemanticException
     */
    public Expr ascribe(Expr e, Type toType) throws SemanticException {
        return e;
    }

    // TODO is this comment revealing too much implementation?
    /** Calls {@code ascribe()} with the expected type and expression
     *  as appropriate. Otherwise functionally the same as the
     *  {@code leaveCall} method in {@code ErrorHandlingVisitor}.
     */
    @Override
    public Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {

        if (n instanceof Expr) {
            Expr e = (Expr) n;
            Type type = ((AscriptionVisitor) v).type;
            return ascribe(e, type);
        }

        return n;
    }
}

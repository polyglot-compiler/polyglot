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
package polyglot.ext.jl8.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import polyglot.ast.ClassMember;
import polyglot.ast.Expr;
import polyglot.ast.Expr_c;
import polyglot.ast.Ext;
import polyglot.ast.LocalDecl;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Precedence;
import polyglot.ast.Term;
import polyglot.ast.TypeNode;
import polyglot.ext.jl8.types.JL8TypeSystem;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeChecker;

public class Lambda_c extends Expr_c implements Lambda {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LambdaFunctionDeclaration declaration;

    //    @Deprecated
    Lambda_c(LambdaFunctionDeclaration declaration) {
        this(declaration, null);
    }

    Lambda_c(LambdaFunctionDeclaration declaration, Ext ext) {
        super(declaration.position(), ext);
        this.declaration = declaration;
    }

    @Override
    public Precedence precedence() {
        return Precedence.LAMBDA;
    }

    @Override
    public LambdaFunctionDeclaration declaration() {
        return this.declaration;
    }

    @Override
    public Lambda declaration(LambdaFunctionDeclaration declaration) {
        return reconstruct(this, declaration);
    }

    protected <N extends Lambda_c> N reconstruct(N n, LambdaFunctionDeclaration declaration) {
        if (n.declaration == declaration) return n;
        n = copyIfNeeded(n);
        n.declaration = declaration;
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        LambdaFunctionDeclaration declaration = visitChild(this.declaration, v);
        return reconstruct(this, declaration);
    }

    @Override
    public Node overrideContextVisit(Node parent, ContextVisitor visitor) throws SemanticException {
        if (parent instanceof LocalDecl) {
            LocalDecl localDecl = (LocalDecl) parent;
            Type type = localDecl.declType();
            if (type.isCanonical()) {
                this.declaration.setTargetType(
                        type,
                        (JL8TypeSystem) visitor.context().typeSystem(),
                        visitor.nodeFactory());
            }
        }
        return super.overrideContextVisit(parent, visitor);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return type(this.declaration.targetType);
    }

    private New equivalentNewCode(NodeFactory nf) {
        MethodInstance sam = this.declaration.sam;
        return nf.New(
                Position.COMPILER_GENERATED,
                nf.CanonicalTypeNode(Position.COMPILER_GENERATED, this.declaration.targetType),
                new ArrayList<Expr>(),
                nf.ClassBody(
                        Position.COMPILER_GENERATED,
                        Collections.<ClassMember>singletonList(
                                nf.MethodDecl(
                                        Position.COMPILER_GENERATED,
                                        sam.flags().clearAbstract(),
                                        nf.CanonicalTypeNode(
                                                Position.COMPILER_GENERATED, sam.returnType()),
                                        nf.Id(Position.COMPILER_GENERATED, sam.name()),
                                        this.declaration.formals(),
                                        new ArrayList<TypeNode>(),
                                        this.declaration.block,
                                        null))));
    }

    @Override
    public void translate(CodeWriter w, Translator tr) {
        lang().translate(this.equivalentNewCode(tr.nodeFactory()), w, tr);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        if (pp instanceof Translator) {
            this.translate(w, (Translator) pp);
            return;
        }
        this.declaration.prettyPrint(w, pp);
    }

    @Override
    public String toString() {
        return this.declaration.toString();
    }

    @Override
    public Term firstChild() {
        return null;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        JL8NodeFactory jl8NodeFactory = (JL8NodeFactory) nf;
        return jl8NodeFactory.Lambda(this.declaration);
    }
}

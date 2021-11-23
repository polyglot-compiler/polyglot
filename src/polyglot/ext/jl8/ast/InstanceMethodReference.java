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
import polyglot.ast.Block;
import polyglot.ast.Call;
import polyglot.ast.ClassMember;
import polyglot.ast.Expr;
import polyglot.ast.Ext;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Term;
import polyglot.ast.Term_c;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.ext.jl8.types.JL8TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class InstanceMethodReference extends Term_c implements FunctionSpec {

    protected Expr receiver;
    protected List<TypeNode> typeArgs;
    protected String methodName;
    protected ReferenceType targetType = null;
    protected MethodInstance sam = null;

    //    @Deprecated
    InstanceMethodReference(
            Position position, Expr receiver, List<TypeNode> typeArgs, String methodName) {
        this(position, receiver, typeArgs, methodName, null);
    }

    public InstanceMethodReference(
            Position position, Expr receiver, List<TypeNode> typeArgs, String methodName, Ext ext) {
        super(position, ext);
        this.receiver = receiver;
        this.typeArgs = typeArgs;
        this.methodName = methodName;
    }

    @Override
    public ReferenceType targetType() {
        return targetType;
    }

    @Override
    public Type temporaryTypeBeforeTypeChecking(JL8TypeSystem ts) {
        return ts.unknownType(Position.COMPILER_GENERATED);
    }

    @Override
    public FunctionSpec withTargetType(
            Type targetType,
            JL8TypeSystem jl8TypeSystem,
            NodeFactory nodeFactory,
            ClassType currentClass)
            throws SemanticException {
        if (targetType.isReference()) {
            ReferenceType targetReferenceType = targetType.toReference();
            List<MethodInstance> methods =
                    jl8TypeSystem.nonObjectPublicAbstractMethods(targetReferenceType);
            if (methods.size() == 1) {
                this.targetType = targetReferenceType;
                this.sam = methods.get(0);
                this.receiver =
                        (Expr)
                                LambdaExpression.replaceThisWithQualifiedThis(
                                        this.receiver, lang(), nodeFactory, currentClass);
                return this;
            }
        }
        throw new SemanticException(targetType + " is not a functional interface.", position());
    }

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc) throws SemanticException {
        if (this.targetType != null) return super.typeCheckOverride(parent, tc);
        return this; // Not ready for type checking!
    }

    protected <N extends InstanceMethodReference> N receiver(N n, Expr receiver) {
        if (n.receiver == receiver) return n;
        n = copyIfNeeded(n);
        n.receiver = receiver;
        return n;
    }

    protected <N extends InstanceMethodReference> N typeArgs(N n, List<TypeNode> typeArgs) {
        if (n.typeArgs == typeArgs) return n;
        n = copyIfNeeded(n);
        n.typeArgs = typeArgs;
        return n;
    }

    @Override
    public Term firstChild() {
        return receiver;
    }

    @Override
    public New equivalentNewCode(NodeFactory nodeFactory) {
        MethodInstance method = this.sam;
        String uniqueNameSuffix = "__" + System.currentTimeMillis();
        List<? extends Type> formalTypes = method.formalTypes();
        List<Formal> formals = new ArrayList<>(formalTypes.size());
        List<Expr> args = new ArrayList<>(formalTypes.size());
        for (int i = 0; i < formalTypes.size(); i++) {
            Id argName = nodeFactory.Id(Position.COMPILER_GENERATED, "arg" + i + uniqueNameSuffix);
            formals.add(
                    nodeFactory.Formal(
                            this.position,
                            Flags.NONE,
                            nodeFactory.CanonicalTypeNode(
                                    Position.COMPILER_GENERATED, formalTypes.get(i)),
                            argName));
            args.add(nodeFactory.Local(Position.COMPILER_GENERATED, argName));
        }
        Call syntheticCall =
                ((JL5NodeFactory) nodeFactory)
                        .Call(
                                Position.COMPILER_GENERATED,
                                this.receiver,
                                this.typeArgs,
                                nodeFactory.Id(Position.COMPILER_GENERATED, this.methodName),
                                args);
        Block block;
        if (method.returnType().equals(method.typeSystem().Void())) {
            block =
                    nodeFactory.Block(
                            Position.COMPILER_GENERATED,
                            nodeFactory.Eval(Position.COMPILER_GENERATED, syntheticCall));
        } else {
            block =
                    nodeFactory.Block(
                            Position.COMPILER_GENERATED,
                            nodeFactory.Return(Position.COMPILER_GENERATED, syntheticCall));
        }

        return nodeFactory.New(
                Position.COMPILER_GENERATED,
                nodeFactory.CanonicalTypeNode(Position.COMPILER_GENERATED, this.targetType),
                new ArrayList<Expr>(),
                nodeFactory.ClassBody(
                        Position.COMPILER_GENERATED,
                        Collections.<ClassMember>singletonList(
                                nodeFactory.MethodDecl(
                                        Position.COMPILER_GENERATED,
                                        sam.flags().clearAbstract(),
                                        nodeFactory.CanonicalTypeNode(
                                                Position.COMPILER_GENERATED, sam.returnType()),
                                        nodeFactory.Id(Position.COMPILER_GENERATED, sam.name()),
                                        formals,
                                        new ArrayList<TypeNode>(),
                                        block,
                                        null))));
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr receiver = visitChild(this.receiver, v);
        List<TypeNode> typeArgs = visitList(this.typeArgs, v);
        return typeArgs(receiver(this, receiver), typeArgs);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFGList(this.typeArgs, this.receiver, ENTRY);
        v.visitCFG(this.receiver, this, EXIT);
        return succs;
    }

    @Override
    public String toString() {
        if (typeArgs.isEmpty()) return receiver + "::" + methodName;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < typeArgs.size(); i++) {
            sb.append(typeArgs.get(i));
            if (i < typeArgs.size() - 1) {
                sb.append(", ");
            }
        }
        return receiver + "::<" + sb + ">" + methodName;
    }
}

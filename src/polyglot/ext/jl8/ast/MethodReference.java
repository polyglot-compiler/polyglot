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
import polyglot.ast.ClassMember;
import polyglot.ast.Expr;
import polyglot.ast.Ext;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Receiver;
import polyglot.ast.Special;
import polyglot.ast.Term;
import polyglot.ast.Term_c;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl8.types.JL8TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class MethodReference extends Term_c implements FunctionSpec {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Receiver receiver;
    protected List<TypeNode> typeArgs;
    protected String methodName;
    protected ReferenceType targetType = null;
    protected MethodInstance sam = null;

    private boolean includeReceiverAsParameter = false;

    //    @Deprecated
    MethodReference(
            Position position, Receiver receiver, List<TypeNode> typeArgs, String methodName) {
        this(position, receiver, typeArgs, methodName, null);
    }

    public MethodReference(
            Position position,
            Receiver receiver,
            List<TypeNode> typeArgs,
            String methodName,
            Ext ext) {
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
                        (Receiver)
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

    private List<ReferenceType> actualTypeArgs() {
        List<ReferenceType> actualTypeArgs = new ArrayList<>(this.typeArgs.size());
        for (TypeNode tn : this.typeArgs) {
            actualTypeArgs.add((ReferenceType) tn.type());
        }
        return actualTypeArgs;
    }

    private ReferenceType findTargetType() throws SemanticException {
        Type t = this.receiver.type();
        if (t.isReference()) {
            return t.toReference();
        } else {
            // trying to invoke a method on a non-reference type.
            // let's pull out an appropriate error message.
            if (this.receiver instanceof Expr) {
                throw new SemanticException(
                        "Cannot invoke method \""
                                + this.methodName
                                + "\" on "
                                + "an expression of non-reference type "
                                + t
                                + ".",
                        this.receiver.position());
            } else if (this.receiver instanceof TypeNode) {
                throw new SemanticException(
                        "Cannot invoke static method \""
                                + this.methodName
                                + "\" on non-reference type "
                                + t
                                + ".",
                        this.receiver.position());
            }
            throw new SemanticException(
                    "Cannot invoke method \""
                            + this.methodName
                            + "\" on non-reference type "
                            + t
                            + ".",
                    this.receiver.position());
        }
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL8TypeSystem ts = (JL8TypeSystem) tc.typeSystem();
        Context c = tc.context();

        if (!this.receiver.type().isCanonical()) return this;
        List<Type> argTypes = new ArrayList<>(this.sam.formalTypes());

        if (this.methodName.equals("new")) {
            if (!(this.receiver instanceof TypeNode)) {
                throw new SemanticException("Receiver is not a type.", this.receiver.position());
            }
            TypeNode typeNodeReceiver = (TypeNode) this.receiver;
            if (typeNodeReceiver.type().isArray()) {
                if (argTypes.size() != 1) {
                    throw new SemanticException(
                            String.format(
                                    "Incompatible parameter types in method: wrong"
                                            + " number of parameters: expected %d but found 1",
                                    argTypes.size()),
                            position());
                }
                Type expectedArgType = argTypes.get(0);
                if (!ts.isImplicitCastValid(ts.Int(), expectedArgType)) {
                    throw new SemanticException(
                            String.format(
                                    "Incompatible parameter types in method reference:"
                                            + " expected %s but found %s",
                                    expectedArgType, ts.Int()),
                            position());
                }
                if (!ts.equals(typeNodeReceiver.type(), this.sam.returnType())) {
                    throw new SemanticException(
                            String.format(
                                    "Incompatible return type in method reference:"
                                            + " expected %s but found %s",
                                    this.sam.returnType(), typeNodeReceiver.type()),
                            position());
                }
            } else if (typeNodeReceiver.type().isClass()) {
                ClassType ct = typeNodeReceiver.type().toClass();
                ConstructorInstance ci =
                        ts.findConstructor(ct, argTypes, actualTypeArgs(), c.currentClass(), true);

                int expectedSize = argTypes.size();
                if (expectedSize != ci.formalTypes().size()) {
                    throw new SemanticException(
                            String.format(
                                    "Incompatible parameter types in lambda expression: wrong"
                                            + " number of parameters: expected %d but found %d",
                                    expectedSize, ci.formalTypes().size()),
                            position());
                }
                for (int i = 0; i < expectedSize; i++) {
                    Type expectedType = argTypes.get(i);
                    Type actualType = ci.formalTypes().get(i);
                    if (!ts.equals(actualType, expectedType)) {
                        throw new SemanticException(
                                String.format(
                                        "Incompatible parameter types in method reference:"
                                                + " expected %s but found %s",
                                        expectedType, actualType),
                                position());
                    }
                }
                if (!ts.equals(ci.container(), this.sam.returnType())) {
                    throw new SemanticException(
                            String.format(
                                    "Incompatible return type in method reference:"
                                            + " expected %s but found %s",
                                    this.sam.returnType(), ci.container()),
                            position());
                }
            } else {
                throw new SemanticException(
                        "Receiver is not a class or array type.", this.receiver.position());
            }
            return this;
        }

        List<ReferenceType> actualTypeArgs = actualTypeArgs();

        ReferenceType targetType = findTargetType();

        /* This call is in a static context if and only if
         * the target (possibly implicit) is a type node.
         */
        boolean staticContext = (this.receiver instanceof TypeNode);

        if (staticContext && targetType instanceof RawClass) {
            targetType = ((RawClass) targetType).base();
        }

        JL5MethodInstance mi =
                (JL5MethodInstance)
                        ts.findMethodForMethodReference(
                                targetType,
                                this.methodName,
                                argTypes,
                                actualTypeArgs,
                                c.currentClass(),
                                this.sam.returnType(),
                                !(this.receiver instanceof Special));

        this.includeReceiverAsParameter = staticContext && !mi.flags().isStatic();

        // If the target is super, but the method is abstract, then complain.
        if (this.receiver instanceof Special
                && ((Special) this.receiver).kind() == Special.SUPER
                && mi.flags().isAbstract()) {
            throw new SemanticException(
                    "Cannot call an abstract method " + "of the super class", this.position());
        }

        int expectedSize = argTypes.size();
        if (this.includeReceiverAsParameter) {
            expectedSize -= 1;
            argTypes.remove(0);
        }
        if (expectedSize != mi.formalTypes().size()) {
            throw new SemanticException(
                    String.format(
                            "Incompatible parameter types in lambda expression: wrong"
                                    + " number of parameters: expected %d but found %d",
                            expectedSize, mi.formalTypes().size()),
                    position());
        }
        for (int i = 0; i < expectedSize; i++) {
            Type expectedType = argTypes.get(i);
            Type actualType = mi.formalTypes().get(i);
            if (!ts.equals(actualType, expectedType)) {
                throw new SemanticException(
                        String.format(
                                "Incompatible parameter types in method reference:"
                                        + " expected %s but found %s",
                                expectedType, actualType),
                        position());
            }
        }
        if (!ts.equals(mi.returnType(), this.sam.returnType())) {
            throw new SemanticException(
                    String.format(
                            "Incompatible return type in method reference:"
                                    + " expected %s but found %s",
                            this.sam.returnType(), mi.returnType()),
                    position());
        }

        return this;
    }

    protected <N extends MethodReference> N receiver(N n, Receiver receiver) {
        if (n.receiver == receiver) return n;
        n = copyIfNeeded(n);
        n.receiver = receiver;
        return n;
    }

    protected <N extends MethodReference> N typeArgs(N n, List<TypeNode> typeArgs) {
        if (n.typeArgs == typeArgs) return n;
        n = copyIfNeeded(n);
        n.typeArgs = typeArgs;
        return n;
    }

    @Override
    public Term firstChild() {
        if (this.receiver instanceof Term) return (Term) this.receiver;
        return null;
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
        Expr syntheticCall;
        if (this.methodName.equals("new")) {
            if (this.receiver.type().isArray()) {
                syntheticCall =
                        nodeFactory.NewArray(
                                Position.COMPILER_GENERATED,
                                nodeFactory.CanonicalTypeNode(
                                        Position.COMPILER_GENERATED,
                                        this.receiver.type().toArray().base()),
                                Collections.<Expr>singletonList(args.get(0)));
            } else {
                syntheticCall =
                        ((JL5NodeFactory) nodeFactory)
                                .New(
                                        Position.COMPILER_GENERATED,
                                        this.typeArgs,
                                        (TypeNode) this.receiver,
                                        args,
                                        null);
            }
        } else if (this.includeReceiverAsParameter) {
            syntheticCall =
                    ((JL5NodeFactory) nodeFactory)
                            .Call(
                                    Position.COMPILER_GENERATED,
                                    args.get(0),
                                    this.typeArgs,
                                    nodeFactory.Id(Position.COMPILER_GENERATED, this.methodName),
                                    args.subList(1, args.size()));
        } else {
            syntheticCall =
                    ((JL5NodeFactory) nodeFactory)
                            .Call(
                                    Position.COMPILER_GENERATED,
                                    this.receiver,
                                    this.typeArgs,
                                    nodeFactory.Id(Position.COMPILER_GENERATED, this.methodName),
                                    args);
        }
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
        Receiver receiver = visitChild(this.receiver, v);
        List<TypeNode> typeArgs = visitList(this.typeArgs, v);
        return typeArgs(receiver(this, receiver), typeArgs);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (this.receiver instanceof Term) {
            Term receiver = (Term) this.receiver;
            v.visitCFGList(this.typeArgs, receiver, ENTRY);
            v.visitCFG(receiver, this, EXIT);
        } else {
            v.visitCFGList(this.typeArgs, this, EXIT);
        }
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

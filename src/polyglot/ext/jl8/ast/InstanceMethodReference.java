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
import java.util.List;
import polyglot.ast.Expr;
import polyglot.ast.Ext;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Term;
import polyglot.ast.Term_c;
import polyglot.ast.TypeNode;
import polyglot.ext.jl8.types.FunctionType;
import polyglot.ext.jl8.types.JL8TypeSystem;
import polyglot.types.CodeInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class InstanceMethodReference extends Term_c implements FunctionSpec {

    protected Expr target;
    protected List<TypeNode> typeArgs;
    protected String methodName;
    protected ReferenceType targetType = null;
    protected MethodInstance sam = null;

    //    @Deprecated
    InstanceMethodReference(
            Position position, Expr target, List<TypeNode> typeArgs, String methodName) {
        this(position, target, typeArgs, methodName, null);
    }

    public InstanceMethodReference(
            Position position, Expr target, List<TypeNode> typeArgs, String methodName, Ext ext) {
        super(position, ext);
        this.target = target;
        this.typeArgs = typeArgs;
        this.methodName = methodName;
    }

    @Override
    public Term codeBody() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CodeInstance codeInstance() {
        return sam;
    }

    @Override
    public ReferenceType targetType() {
        return targetType;
    }

    @Override
    public Type returnValueType() {
        return sam.returnType();
    }

    private MethodInstance getSAM(TypeSystem ts) {
        if (sam != null) return sam;
        return ts.methodInstance(
                position(),
                targetType,
                Flags.NONE,
                ts.unknownType(position()),
                "",
                new ArrayList<Type>(),
                new ArrayList<Type>());
    }

    @Override
    public FunctionType temporaryTypeBeforeTypeChecking(JL8TypeSystem ts) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTargetType(Type targetType, JL8TypeSystem jl8TypeSystem, NodeFactory nodeFactory)
            throws SemanticException {
        if (targetType.isReference()) {
            ReferenceType targetReferenceType = targetType.toReference();
            List<MethodInstance> methods =
                    jl8TypeSystem.nonObjectPublicAbstractMethods(targetReferenceType);
            if (methods.size() == 1) {
                this.targetType = targetReferenceType;
                MethodInstance method = methods.get(0);
                this.sam = method;
                throw new UnsupportedOperationException();
            }
        }
        throw new SemanticException(targetType + " is not a functional interface.", position());
    }

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc) throws SemanticException {
        if (this.targetType != null) return super.typeCheckOverride(parent, tc);
        return this; // Not ready for type checking!
    }

    protected <N extends InstanceMethodReference> N target(N n, Expr target) {
        if (n.target == target) return n;
        n = copyIfNeeded(n);
        n.target = target;
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
        return target;
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushCode(getSAM(c.typeSystem()));
    }

    @Override
    public New equivalentNewCode(NodeFactory nf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr target = visitChild(this.target, v);
        List<TypeNode> typeArgs = visitList(this.typeArgs, v);
        return typeArgs(target(this, target), typeArgs);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFGList(this.typeArgs, this.target, ENTRY);
        v.visitCFG(this.target, this, EXIT);
        return succs;
    }

    @Override
    public String toString() {
        if (typeArgs.isEmpty()) return target + "::" + methodName;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < typeArgs.size(); i++) {
            sb.append(typeArgs.get(i));
            if (i < typeArgs.size() - 1) {
                sb.append(", ");
            }
        }
        return target + "::<" + sb + ">" + methodName;
    }
}

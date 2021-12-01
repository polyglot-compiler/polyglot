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
import polyglot.ast.Assign;
import polyglot.ast.Call;
import polyglot.ast.CallOps;
import polyglot.ast.Expr;
import polyglot.ast.FieldDecl;
import polyglot.ast.Lang;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.Return;
import polyglot.ast.Special;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5CallExt;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl8.types.JL8TypeSystem;
import polyglot.types.CodeInstance;
import polyglot.types.Context;
import polyglot.types.FunctionInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;

public class JL8CallExt extends JL8ProcedureCallExt implements CallOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public JL8CallExt() {
        this(null);
    }

    public JL8CallExt(List<TypeNode> typeArgs) {
        super(typeArgs);
    }

    @Override
    public Call node() {
        return (Call) super.node();
    }

    private transient Type expectedReturnType = null;

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc) throws SemanticException {
        JL8TypeSystem ts = (JL8TypeSystem) tc.typeSystem();
        Context c = tc.context();

        Call node = (Call) typeArgs(visitList(typeArgs(), tc));
        node =
                node.target() == null
                        ? node
                        : node.target(
                                tc.rethrowMissingDependencies(true).visitEdge(node, node.target()));
        JL8CallExt ext = (JL8CallExt) JL8Ext.ext(node);
        JL5CallExt jl5CallExt = (JL5CallExt) JL5Ext.ext(node);
        List<Expr> partiallyTypeCheckedArguments = new ArrayList<>(node.arguments().size());
        List<Type> argTypes = new ArrayList<>(node.arguments().size());
        for (Expr argument : node.arguments()) {
            Expr checked;
            if (argument instanceof FunctionValue) {
                FunctionValue functionValue = (FunctionValue) argument;
                checked =
                        argument.type(
                                functionValue.functionSpec().temporaryTypeBeforeTypeChecking(ts));
            } else {
                checked = tc.rethrowMissingDependencies(true).visitEdge(node, argument);
            }
            partiallyTypeCheckedArguments.add(checked);
            argTypes.add(checked.type());
        }

        if (parent instanceof Return) {
            CodeInstance ci = tc.context().currentCode();
            if (ci instanceof FunctionInstance) {
                Type type = ((FunctionInstance) ci).returnType();
                if (type == null || !type.isCanonical()) return node;
                ext.expectedReturnType = type;
                jl5CallExt.setExpectedReturnType(type);
            }
        }
        if (parent instanceof Assign) {
            Assign a = (Assign) parent;
            if (this.node() == a.right()) {
                Type type = a.left().type();
                if (type == null || !type.isCanonical()) return node;
                ext.expectedReturnType = type;
                jl5CallExt.setExpectedReturnType(type);
            }
        }
        if (parent instanceof LocalDecl) {
            LocalDecl ld = (LocalDecl) parent;
            Type type = ld.type().type();
            if (type == null || !type.isCanonical()) return node;
            ext.expectedReturnType = type;
            jl5CallExt.setExpectedReturnType(type);
        }
        if (parent instanceof FieldDecl) {
            FieldDecl fd = (FieldDecl) parent;
            Type type = fd.type().type();
            if (type == null || !type.isCanonical()) return node;
            ext.expectedReturnType = type;
            jl5CallExt.setExpectedReturnType(type);
        }

        if (node.target() == null) {
            return lang().typeCheckNullTarget(node, tc, argTypes);
        }
        if (node.target().type() == null || !node.target().type().isCanonical()) {
            return node;
        }
        Call call = (Call) node.arguments(partiallyTypeCheckedArguments);

        List<ReferenceType> actualTypeArgs = actualTypeArgs();

        ReferenceType targetType = tc.lang().findTargetType(call);

        /* This call is in a static context if and only if
         * the target (possibly implicit) is a type node.
         */
        boolean staticContext = (call.target() instanceof TypeNode);

        if (staticContext && targetType instanceof RawClass) {
            targetType = ((RawClass) targetType).base();
        }

        JL5MethodInstance mi =
                (JL5MethodInstance)
                        ts.findMethod(
                                targetType,
                                call.name(),
                                argTypes,
                                actualTypeArgs,
                                c.currentClass(),
                                ext.expectedReturnType,
                                !(call.target() instanceof Special));

        List<Expr> fullyTypeCheckedArguments =
                new ArrayList<>(partiallyTypeCheckedArguments.size());
        for (int i = 0; i < partiallyTypeCheckedArguments.size(); i++) {
            Expr argument = partiallyTypeCheckedArguments.get(i);
            if (argument instanceof FunctionValue) {
                FunctionValue f = (FunctionValue) argument;
                f.setTargetType(mi.formalTypes().get(i), tc);
                fullyTypeCheckedArguments.add(
                        tc.rethrowMissingDependencies(true).visitEdge(node, f));
            } else {
                fullyTypeCheckedArguments.add(argument);
            }
        }
        call = (Call) call.arguments(fullyTypeCheckedArguments);

        if (staticContext && !mi.flags().isStatic()) {
            throw new SemanticException(
                    "Cannot call non-static method "
                            + call.name()
                            + " of "
                            + call.target().type()
                            + " in static "
                            + "context.",
                    call.position());
        }

        // If the target is super, but the method is abstract, then complain.
        if (call.target() instanceof Special
                && ((Special) call.target()).kind() == Special.SUPER
                && mi.flags().isAbstract()) {
            throw new SemanticException(
                    "Cannot call an abstract method " + "of the super class", call.position());
        }

        Type returnType = computeReturnType(mi);

        call = (Call) call.methodInstance(mi).type(returnType);

        // Need to deal with Object.getClass() specially. See JLS 3rd ed., section 4.3.2
        if (mi.name().equals("getClass") && mi.formalTypes().isEmpty()) {
            // the return type of the call is "Class<? extends |T|>" where T is the static type of
            // the receiver.
            Type t = call.target().type();
            ReferenceType et = (ReferenceType) ts.erasureType(t);
            ReferenceType wt = ts.wildCardType(call.position(), et, null);
            Type instClass =
                    ts.instantiate(
                            call.position(),
                            (JL5ParsedClassType) ts.Class(),
                            Collections.singletonList(wt));
            call = (Call) call.type(instClass);
        }

        return call;
    }

    private Type computeReturnType(JL5MethodInstance mi) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) mi.typeSystem();
        if (mi.returnType().isVoid()) return ts.Void();
        return ts.applyCaptureConversion(mi.returnType(), this.node().position());
    }

    @Override
    public Type findContainer(TypeSystem ts, MethodInstance mi) {
        JL5TypeSystem jts = (JL5TypeSystem) ts;
        return jts.erasureType(mi.container());
    }

    @Override
    public ReferenceType findTargetType() throws SemanticException {
        return superLang().findTargetType(node());
    }

    @Override
    public Node typeCheckNullTarget(TypeChecker tc, List<Type> argTypes) throws SemanticException {
        return superLang().typeCheckNullTarget(node(), tc, argTypes);
    }

    @Override
    public boolean constantValueSet(Lang lang) {
        return superLang().constantValueSet(node(), lang);
    }

    @Override
    public boolean isConstant(Lang lang) {
        return superLang().isConstant(node(), lang);
    }

    @Override
    public Object constantValue(Lang lang) {
        return superLang().constantValue(node(), lang);
    }
}

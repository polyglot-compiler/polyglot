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
import polyglot.ast.ConstructorCall;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5ConstructorCallExt;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl8.types.JL8TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;

import static polyglot.ast.ConstructorCall.SUPER;

public class JL8ConstructorCallExt extends JL8ProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public JL8ConstructorCallExt() {
        this(null);
    }

    public JL8ConstructorCallExt(List<TypeNode> typeArgs) {
        super(typeArgs);
    }

    @Override
    public ConstructorCall node() {
        return (ConstructorCall) super.node();
    }

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc_) throws SemanticException {
        TypeChecker tc = (TypeChecker) tc_.enter(parent, node());
        JL8TypeSystem ts = (JL8TypeSystem) tc.typeSystem();
        Context c = tc.context();
        ClassType ct = c.currentClass();

        ConstructorCall call = node();
        ConstructorCall.Kind kind = call.kind();
        if (kind == SUPER) ct = ct.superType().toClass();

        List<Expr> partiallyTypeCheckedArguments = new ArrayList<>(call.arguments().size());
        List<Type> argTypes = new ArrayList<>(call.arguments().size());
        for (Expr argument : call.arguments()) {
            Expr checked;
            if (argument instanceof FunctionValue) {
                FunctionValue functionValue = (FunctionValue) argument;
                checked =
                        argument.type(
                                functionValue.functionSpec().temporaryTypeBeforeTypeChecking(ts));
            } else {
                checked = tc.rethrowMissingDependencies(true).visitEdge(call, argument);
                if (!checked.isDisambiguated()) return call;
            }
            partiallyTypeCheckedArguments.add(checked);
            argTypes.add(checked.type());
        }
        call =
                call.qualifier() == null
                        ? call
                        : call.qualifier(
                                tc.rethrowMissingDependencies(true)
                                        .visitEdge(call, call.qualifier()));
        call = (ConstructorCall) call.arguments(partiallyTypeCheckedArguments);

        ConstructorInstance ci =
                ts.findConstructor(ct, argTypes, actualTypeArgs(), c.currentClass(), false);

        List<Expr> fullyTypeCheckedArguments =
                new ArrayList<>(partiallyTypeCheckedArguments.size());
        for (int i = 0; i < partiallyTypeCheckedArguments.size(); i++) {
            Expr argument = partiallyTypeCheckedArguments.get(i);
            if (argument instanceof FunctionValue) {
                FunctionValue f = (FunctionValue) argument;
                Type lambdaTargetType = ci.formalTypes().get(i);
                f.setTargetType(lambdaTargetType, tc);
                fullyTypeCheckedArguments.add(
                        tc.rethrowMissingDependencies(true).visitEdge(call, f));
            } else {
                fullyTypeCheckedArguments.add(argument);
            }
        }
        call = (ConstructorCall) call.constructorInstance(ci).arguments(fullyTypeCheckedArguments);

        return ((JL5ConstructorCallExt) JL5Ext.ext(call)).typeCheck(tc, call);
    }
}

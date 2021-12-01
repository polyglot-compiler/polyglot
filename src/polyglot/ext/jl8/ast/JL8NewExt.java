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
import polyglot.ast.New;
import polyglot.ast.New_c;
import polyglot.ast.New_c.NewChildVisitor;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.ast.JL5NewExt;
import polyglot.ext.jl7.ast.JL7Ext;
import polyglot.ext.jl7.ast.JL7NewExt;
import polyglot.ext.jl8.types.JL8TypeSystem;
import polyglot.types.ConstructorInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;

public class JL8NewExt extends JL8ProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public JL8NewExt() {
        this(null);
    }

    public JL8NewExt(List<TypeNode> typeArgs) {
        super(typeArgs);
    }

    @Override
    public New node() {
        return (New) super.node();
    }

    @Override
    public Node typeCheckOverride(Node parent, final TypeChecker tc) throws SemanticException {
        final New n = this.node();
        final JL7NewExt ext7 = (JL7NewExt) JL7Ext.ext(n);
        final JL5NewExt ext5 = (JL5NewExt) JL5Ext.ext(n);
        final JL8TypeSystem ts = (JL8TypeSystem) tc.typeSystem();
        if (!ext7.setExpectedObjectTypeFromParent(parent, tc)) {
            return n;
        }
        return New_c.typeCheckOverride(
                (New_c) ext5.typeArgs(n, visitList(ext5.typeArgs(), tc)),
                parent,
                tc,
                new NewChildVisitor() {
                    @Override
                    public New visitArguments(New n, TypeChecker tc) {
                        List<Expr> partiallyTypeCheckedArguments =
                                new ArrayList<>(n.arguments().size());
                        for (Expr argument : n.arguments()) {
                            Expr checked;
                            if (argument instanceof FunctionValue) {
                                FunctionValue functionValue = (FunctionValue) argument;
                                checked =
                                        argument.type(
                                                functionValue
                                                        .functionSpec()
                                                        .temporaryTypeBeforeTypeChecking(ts));
                            } else {
                                checked =
                                        tc.rethrowMissingDependencies(true).visitEdge(n, argument);
                            }
                            partiallyTypeCheckedArguments.add(checked);
                        }
                        return n.arguments(partiallyTypeCheckedArguments);
                    }

                    @Override
                    public New typeCheck(New n, New old, TypeChecker tc) throws SemanticException {
                        n = (New) ext7.typeCheck(tc, n);
                        List<Expr> fullyTypeCheckedArguments =
                                new ArrayList<>(n.arguments().size());
                        ConstructorInstance ci = n.constructorInstance();
                        for (int i = 0; i < n.arguments().size(); i++) {
                            Expr argument = n.arguments().get(i);
                            if (argument instanceof FunctionValue) {
                                FunctionValue f = (FunctionValue) argument;
                                Type lambdaTargetType = ci.formalTypes().get(i);
                                f.setTargetType(lambdaTargetType, tc);
                                fullyTypeCheckedArguments.add(
                                        tc.rethrowMissingDependencies(true).visitEdge(n, f));
                            } else {
                                fullyTypeCheckedArguments.add(argument);
                            }
                        }
                        n = n.arguments(fullyTypeCheckedArguments);
                        return n;
                    }
                });
    }
}

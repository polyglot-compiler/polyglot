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
package polyglot.ext.jl7.ast;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Ambiguous;
import polyglot.ast.Assign;
import polyglot.ast.Expr;
import polyglot.ast.FieldDecl;
import polyglot.ast.LocalDecl;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.Return;
import polyglot.ast.Special;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.ast.JL5NewExt;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl7.types.JL7TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.CodeInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.FunctionInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;

public class JL7NewExt extends JL7Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc) {
        JL7NewExt ext = (JL7NewExt) JL7Ext.ext(this.node());
        if (parent instanceof Return) {
            CodeInstance ci = tc.context().currentCode();
            if (ci instanceof FunctionInstance) {
                ext.setExpectedObjectType(((FunctionInstance) ci).returnType());
            }
        }
        if (parent instanceof Assign) {
            Assign a = (Assign) parent;
            if (this.node() == a.right()) {
                Type type = a.left().type();
                if (type == null || !type.isCanonical()) {
                    // not ready yet
                    return this.node();
                }
                ext.setExpectedObjectType(type);
            }
        }
        if (parent instanceof LocalDecl) {
            LocalDecl ld = (LocalDecl) parent;
            Type type = ld.type().type();
            if (type == null || !type.isCanonical()) {
                // not ready yet
                return this.node();
            }
            ext.setExpectedObjectType(type);
        }
        if (parent instanceof FieldDecl) {
            FieldDecl fd = (FieldDecl) parent;
            Type type = fd.type().type();
            if (type == null || !type.isCanonical()) {
                // not ready yet
                return this.node();
            }
            ext.setExpectedObjectType(type);
        }

        return null;
    }

    private transient Type expectedObjectType = null;

    protected Type expectedObjectType() {
        return this.expectedObjectType;
    }

    protected void setExpectedObjectType(Type type) {
        if (type == null || !type.isCanonical()) {
            expectedObjectType = null;
            return;
        }
        expectedObjectType = type;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        New n = (New) this.node();
        TypeNode objectType = n.objectType();
        if (!(objectType instanceof Ambiguous))
            return superLang().typeCheck(this.node(), tc);

        JL5NewExt ext5 = (JL5NewExt) JL5Ext.ext(n);
        JL7TypeSystem ts = (JL7TypeSystem) tc.typeSystem();

        if (!n.objectType().type().isClass()) {
            throw new SemanticException("Must have a class for a new expression.",
                                        n.position());
        }

        List<Type> argTypes = new ArrayList<Type>(n.arguments().size());

        for (Expr e : n.arguments()) {
            argTypes.add(e.type());
        }

        List<ReferenceType> actualTypeArgs =
                new ArrayList<ReferenceType>(ext5.typeArgs().size());
        for (TypeNode tn : ext5.typeArgs()) {
            actualTypeArgs.add((ReferenceType) tn.type());
        }

        superLang().typeCheckFlags(this.node(), tc);
        superLang().typeCheckNested(this.node(), tc);

        if (n.body() != null) {
            ts.checkClassConformance(n.anonType());
        }

        ClassType ct = n.objectType().type().toClass();

        if (ct.isInnerClass()) {
            ClassType outer = ct.outer();
            JL5TypeSystem ts5 = (JL5TypeSystem) tc.typeSystem();
            if (outer instanceof JL5SubstClassType) {
                JL5SubstClassType sct = (JL5SubstClassType) outer;
                ct = (ClassType) sct.subst().substType(ct);
            }
            else if (n.qualifier() == null
                    || (n.qualifier() instanceof Special && ((Special) n.qualifier()).kind() == Special.THIS)) {
                ct = ts5.instantiateInnerClassFromContext(tc.context(), ct);
            }
            else if (n.qualifier().type() instanceof JL5SubstClassType) {
                JL5SubstClassType sct =
                        (JL5SubstClassType) n.qualifier().type();
                ct = (ClassType) sct.subst().substType(ct);
            }
        }

        ConstructorInstance ci;
        if (!ct.flags().isInterface()) {
            Context c = tc.context();
            if (n.anonType() != null) {
                c = c.pushClass(n.anonType(), n.anonType());
            }
            if (ct instanceof JL5ParsedClassType
                    && !((JL5ParsedClassType) ct).typeVariables().isEmpty())
                ci =
                        ts.findConstructor(ct,
                                           argTypes,
                                           actualTypeArgs,
                                           c.currentClass(),
                                           this.expectedObjectType());
            else ci =
                    ts.findConstructor(ct,
                                       argTypes,
                                       actualTypeArgs,
                                       c.currentClass());
        }
        else {
            ci = ts.defaultConstructor(n.position(), ct);
        }

        n = n.constructorInstance(ci);

        if (n.anonType() != null) {
            // The type of the new expression is the anonymous type, not the base type.
            ct = n.anonType();
        }

        return n.type(ct);
    }
}

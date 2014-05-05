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
import java.util.Collections;
import java.util.List;

import polyglot.ast.Assign;
import polyglot.ast.Expr;
import polyglot.ast.FieldDecl;
import polyglot.ast.Lang;
import polyglot.ast.LocalDecl;
import polyglot.ast.New;
import polyglot.ast.NewOps;
import polyglot.ast.Node;
import polyglot.ast.Return;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.ast.JL5NewExt;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl7.types.DiamondType;
import polyglot.ext.jl7.types.JL7TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.CodeInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.FunctionInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL7NewExt extends JL7ProcedureCallExt implements NewOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public New node() {
        return (New) super.node();
    }

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException {
        if (parent instanceof Return) {
            CodeInstance ci = tc.context().currentCode();
            if (ci instanceof FunctionInstance) {
                setExpectedObjectType(((FunctionInstance) ci).returnType());
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
                setExpectedObjectType(type);
            }
        }
        if (parent instanceof LocalDecl) {
            LocalDecl ld = (LocalDecl) parent;
            Type type = ld.type().type();
            if (type == null || !type.isCanonical()) {
                // not ready yet
                return this.node();
            }
            setExpectedObjectType(type);
        }
        if (parent instanceof FieldDecl) {
            FieldDecl fd = (FieldDecl) parent;
            Type type = fd.type().type();
            if (type == null || !type.isCanonical()) {
                // not ready yet
                return this.node();
            }
            setExpectedObjectType(type);
        }

        return superLang().typeCheckOverride(node(), parent, tc);
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
        New n = this.node();
        TypeNode objectType = n.objectType();
        if (!(objectType.type() instanceof DiamondType))
            return superLang().typeCheck(this.node(), tc);

        // Type check instance creation expressions using diamond.
        JL5NewExt ext5 = (JL5NewExt) JL5Ext.ext(n);
        List<TypeNode> typeArgs = ext5.typeArgs();
        if (!typeArgs.isEmpty())
            throw new SemanticException("Explicit type arguments cannot be used"
                    + " with '<>' in an allocation expression");
        if (n.body() != null)
            throw new SemanticException("'<>' cannot be used with anonymous classes");

        JL7TypeSystem ts = (JL7TypeSystem) tc.typeSystem();

        if (!objectType.type().isClass()) {
            throw new SemanticException("Must have a class for a new expression.",
                                        n.position());
        }

        List<Type> argTypes = new ArrayList<>(n.arguments().size());

        for (Expr e : n.arguments()) {
            argTypes.add(e.type());
        }

        superLang().typeCheckFlags(this.node(), tc);
        superLang().typeCheckNested(this.node(), tc);

        // Perform overload resolution and type argument inference as specified
        // in JLS SE 7 | 15.9.3.
        DiamondType ct = (DiamondType) objectType.type().toClass();
        Context c = tc.context();

        ConstructorInstance ci =
                ts.findConstructor(ct,
                                   argTypes,
                                   Collections.<ReferenceType> emptyList(),
                                   c.currentClass(),
                                   expectedObjectType(),
                                   n.body() == null);
        ct.inferred((JL5SubstClassType) ci.container());

        n = n.constructorInstance(ci);
        return n.type(ct);
    }

    @Override
    public TypeNode findQualifiedTypeNode(AmbiguityRemover ar, ClassType outer,
            TypeNode objectType) throws SemanticException {
        if (objectType instanceof AmbDiamondTypeNode) {
            JL7TypeSystem ts = (JL7TypeSystem) ar.typeSystem();
            Context c = ar.context();

            // Check for visibility of inner class, but ignore result
            ts.findMemberClass(outer, objectType.name(), c.currentClass());

            if (outer instanceof ParsedClassType) {
                ParsedClassType opct = (ParsedClassType) outer;
                c = c.pushClass(opct, opct);
            }
            else if (outer instanceof JL5SubstClassType) {
                JL5SubstClassType osct = (JL5SubstClassType) outer;
                c = c.pushClass(osct.base(), osct.base());
            }
            else if (outer instanceof RawClass) {
                RawClass orct = (RawClass) outer;
                c = c.pushClass(orct.base(), orct.base());
            }
            return (TypeNode) objectType.visit(ar.context(c));
        }
        return superLang().findQualifiedTypeNode(this.node(),
                                                 ar,
                                                 outer,
                                                 objectType);
    }

    @Override
    public Expr findQualifier(AmbiguityRemover ar, ClassType ct)
            throws SemanticException {
        return superLang().findQualifier(this.node(), ar, ct);
    }

    @Override
    public ClassType findEnclosingClass(Context c, ClassType ct) {
        if (ct instanceof DiamondType) {
            DiamondType dt = (DiamondType) ct;
            ct = dt.base();
        }
        return superLang().findEnclosingClass(this.node(), c, ct);
    }

    @Override
    public void typeCheckFlags(TypeChecker tc) throws SemanticException {
        superLang().typeCheckFlags(this.node(), tc);
    }

    @Override
    public void typeCheckNested(TypeChecker tc) throws SemanticException {
        superLang().typeCheckNested(this.node(), tc);
    }

    @Override
    public void printQualifier(CodeWriter w, PrettyPrinter tr) {
        superLang().printQualifier(this.node(), w, tr);
    }

    @Override
    public void printShortObjectType(CodeWriter w, PrettyPrinter tr) {
        New n = this.node();
        superLang().printShortObjectType(n, w, tr);
        ClassType ct = n.objectType().type().toClass();
        if (ct instanceof DiamondType) w.write("<>");
    }

    @Override
    public void printBody(CodeWriter w, PrettyPrinter tr) {
        superLang().printBody(this.node(), w, tr);
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

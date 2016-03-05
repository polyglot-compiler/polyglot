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
package polyglot.ext.jl5.ast;

import static polyglot.ast.ConstructorCall.SUPER;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorCall_c;
import polyglot.ast.Expr;
import polyglot.ast.IntLit;
import polyglot.ast.Node;
import polyglot.ast.Precedence;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.JL5Options;
import polyglot.ext.jl5.types.JL5Context;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5ConstructorCallExt extends JL5ProcedureCallExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public JL5ConstructorCallExt() {
        this(null, false);
    }

    public JL5ConstructorCallExt(List<TypeNode> typeArgs,
            boolean isEnumConstructorCall) {
        super(typeArgs);
        this.isEnumConstructorCall = isEnumConstructorCall;
    }

    @Override
    public ConstructorCall node() {
        return (ConstructorCall) super.node();
    }

    /**
     * Is this constructor call a super call to java.lang.Enum?
     */
    protected boolean isEnumConstructorCall;

    public boolean isEnumConstructorCall() {
        return isEnumConstructorCall;
    }

    @Override
    public Context enterScope(Context c) {
        return ((JL5Context) c).pushCTORCall();
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        ConstructorCall cc = node();
        JL5ConstructorCallExt ext = (JL5ConstructorCallExt) JL5Ext.ext(cc);
        ClassType ct = ar.context().currentClass();
        if (ct != null && JL5Flags.isEnum(ct.flags())) {
            if (cc.arguments().isEmpty()) {
                // this is an enum decl, so we need to replace a call to the default
                // constructor with a call to java.lang.Enum.Enum(String, int)
                List<Expr> args = new ArrayList<>(2);// XXX the right thing to do is change the type of java.lang.Enum instead of adding these dummy params
                args.add(ar.nodeFactory().NullLit(Position.compilerGenerated()));
                args.add(ar.nodeFactory().IntLit(Position.compilerGenerated(),
                                                 IntLit.INT,
                                                 0));
                cc = (ConstructorCall) cc.arguments(args);
                ext = (JL5ConstructorCallExt) JL5Ext.ext(cc);
                ext.isEnumConstructorCall = true;
                return superLang().disambiguate(cc, ar);
            }
        }
        return superLang().disambiguate(node(), ar);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        ConstructorCall_c n = (ConstructorCall_c) node();

        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        Context c = tc.context();

        ClassType ct = c.currentClass();
        Type superType = ct.superType();

        Expr qualifier = n.qualifier();
        ConstructorCall.Kind kind = n.kind();

        List<ReferenceType> actualTypeArgs = actualTypeArgs();

        // The qualifier specifies the enclosing instance of this inner class.
        // The type of the qualifier must be the outer class of this
        // inner class or one of its super types.
        //
        // Example:
        //
        // class Outer {
        //     class Inner { }
        // }
        //
        // class ChildOfInner extends Outer.Inner {
        //     ChildOfInner() { (new Outer()).super(); }
        // }
        if (qualifier != null) {
            if (!qualifier.isDisambiguated()) {
                return n;
            }

            if (kind != SUPER) {
                throw new SemanticException("Can only qualify a \"super\""
                        + "constructor invocation.", n.position());
            }

            if (!superType.isClass() || !superType.toClass().isInnerClass()
                    || superType.toClass().inStaticContext()) {
                throw new SemanticException("The class \""
                                                    + superType
                                                    + "\""
                                                    + " is not an inner class, or was declared in a static "
                                                    + "context; a qualified constructor invocation cannot "
                                                    + "be used.",
                                            n.position());
            }

            Type qt = qualifier.type();

            if (!qt.isClass() || !qt.isSubtype(superType.toClass().outer())) {
                throw new SemanticException("The type of the qualifier "
                                                    + "\""
                                                    + qt
                                                    + "\" does not match the immediately enclosing "
                                                    + "class  of the super class \""
                                                    + superType.toClass()
                                                               .outer() + "\".",
                                            qualifier.position());
            }
        }

        if (kind == SUPER) {
            if (!superType.isClass()) {
                throw new SemanticException("Super type of " + ct
                        + " is not a class.", n.position());
            }

            Expr q = qualifier;

            // If the super class is an inner class (i.e., has an enclosing
            // instance of its container class), then either a qualifier
            // must be provided, or ct must have an enclosing instance of the
            // super class's container class, or a subclass thereof.
            if (q == null && superType.isClass()
                    && superType.toClass().isInnerClass()) {
                ClassType superContainer = superType.toClass().outer();
                // ct needs an enclosing instance of superContainer,
                // or a subclass of superContainer.
                ClassType e = ct;

                while (e != null) {
                    // use isImplicitCastValid instead of isSubtype in order to allow unchecked conversion.
                    if (e.isImplicitCastValid(superContainer)
                            && ct.hasEnclosingInstance(e)) {
                        break;
                    }
                    e = e.outer();
                }

                if (e == null) {
                    throw new SemanticException(ct
                                                        + " must have an enclosing instance"
                                                        + " that is a subtype of "
                                                        + superContainer,
                                                n.position());
                }
                if (e == ct) {
                    throw new SemanticException(ct
                                                        + " is a subtype of "
                                                        + superContainer
                                                        + "; an enclosing instance that is a subtype of "
                                                        + superContainer
                                                        + " must be specified in the super constructor call.",
                                                n.position());
                }
            }

            // we differ here from the implementation in ConstructorCall_c in that we do not modify the qualifier
        }

        List<Type> argTypes = new LinkedList<>();

        for (Expr e : n.arguments()) {
            if (!e.isDisambiguated()) {
                return n;
            }
            argTypes.add(e.type());
        }

        if (kind == SUPER) {
            ct = ct.superType().toClass();
        }

        ConstructorInstance ci =
                ts.findConstructor(ct,
                                   argTypes,
                                   actualTypeArgs,
                                   c.currentClass(),
                                   false);
        return n.constructorInstance(ci);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        ConstructorCall cc = node();
        JL5ConstructorCallExt ext = (JL5ConstructorCallExt) JL5Ext.ext(cc);

        // are we a super call within an enum const decl?
        if (ext.isEnumConstructorCall() && cc.constructorInstance() != null) {
            boolean translateEnums =
                    ((JL5Options) cc.constructorInstance()
                                    .typeSystem()
                                    .extensionInfo()
                                    .getOptions()).translateEnums;
            boolean removeJava5isms =
                    ((JL5Options) cc.constructorInstance()
                                    .typeSystem()
                                    .extensionInfo()
                                    .getOptions()).removeJava5isms;
            if (!removeJava5isms && translateEnums) {
                // we don't print an explicit call to super
                return;
            }
        }

        if (cc.qualifier() != null) {
            printSubExpr(cc.qualifier(), w, tr);
            w.write(".");
        }

        super.prettyPrint(w, tr);
        w.write(cc.kind().toString());
        printArgs(w, tr);
        w.write(";");
    }

    protected void printSubExpr(Expr expr, CodeWriter w, PrettyPrinter pp) {
        if (Precedence.LITERAL.isTighter(expr.precedence())) {
            w.write("(");
            printBlock(expr, w, pp);
            w.write(")");
        }
        else {
            print(expr, w, pp);
        }
    }
}

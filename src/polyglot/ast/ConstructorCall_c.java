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

package polyglot.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.translate.ExtensionRewriter;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.ProcedureInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A {@code ConstructorCall} represents a direct call to a constructor.
 * For instance, {@code super(...)} or {@code this(...)}.
 */
public class ConstructorCall_c extends Stmt_c implements ConstructorCall,
        ProcedureCallOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Kind kind;
    protected Expr qualifier;
    protected List<Expr> arguments;
    protected ConstructorInstance ci;

//    @Deprecated
    public ConstructorCall_c(Position pos, Kind kind, Expr qualifier,
            List<? extends Expr> arguments) {
        this(pos, kind, qualifier, arguments, null);
    }

    public ConstructorCall_c(Position pos, Kind kind, Expr qualifier,
            List<? extends Expr> arguments, Ext ext) {
        super(pos, ext);
        assert (kind != null && arguments != null); // qualifier may be null
        this.kind = kind;
        this.qualifier = qualifier;
        this.arguments = ListUtil.copy(arguments, true);
    }

    @Override
    public Expr qualifier() {
        return this.qualifier;
    }

    @Override
    public ConstructorCall qualifier(Expr qualifier) {
        return qualifier(this, qualifier);
    }

    protected <N extends ConstructorCall_c> N qualifier(N n, Expr qualifier) {
        if (n.qualifier == qualifier) return n;
        n = copyIfNeeded(n);
        n.qualifier = qualifier;
        return n;
    }

    @Override
    public Kind kind() {
        return this.kind;
    }

    @Override
    public ConstructorCall kind(Kind kind) {
        return kind(this, kind);
    }

    protected <N extends ConstructorCall_c> N kind(N n, Kind kind) {
        if (n.kind == kind) return n;
        n = copyIfNeeded(n);
        n.kind = kind;
        return n;
    }

    @Override
    public List<Expr> arguments() {
        return this.arguments;
    }

    @Override
    public ProcedureCall arguments(List<Expr> arguments) {
        return arguments(this, arguments);
    }

    protected <N extends ConstructorCall_c> N arguments(N n,
            List<Expr> arguments) {
        if (CollectionUtil.equals(n.arguments, arguments)) return n;
        n = copyIfNeeded(n);
        n.arguments = ListUtil.copy(arguments, true);
        return n;
    }

    @Override
    public ProcedureInstance procedureInstance() {
        return constructorInstance();
    }

    @Override
    public ConstructorInstance constructorInstance() {
        return ci;
    }

    @Override
    public ConstructorCall constructorInstance(ConstructorInstance ci) {
        return constructorInstance(this, ci);
    }

    protected <N extends ConstructorCall_c> N constructorInstance(N n,
            ConstructorInstance ci) {
        if (n.ci == ci) return n;
        n = copyIfNeeded(n);
        n.ci = ci;
        return n;
    }

    /**
     * An explicit constructor call is a static context. We need to record
     * this.
     */
    @Override
    public Context enterScope(Context c) {
        return c.pushStatic();
    }

    /** Reconstruct the constructor call. */
    protected <N extends ConstructorCall_c> N reconstruct(N n, Expr qualifier,
            List<Expr> arguments) {
        n = qualifier(n, qualifier);
        n = arguments(n, arguments);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr qualifier = visitChild(this.qualifier, v);
        List<Expr> arguments = visitList(this.arguments, v);
        return reconstruct(this, qualifier, arguments);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        TypeSystem ts = tb.typeSystem();

        // Remove super() calls for java.lang.Object.
        // XXX Avoid using ts.Object(), which might throw MissingDependencyException,
        // and cause everything before the constructor declaration to be added twice.
        if (kind == SUPER
                && tb.currentClass().fullName().equals("java.lang.Object")) {
            return tb.nodeFactory().Empty(position());
        }

        ConstructorCall_c n = (ConstructorCall_c) super.buildTypes(tb);

        List<Type> l = new ArrayList<>(arguments.size());
        for (int i = 0; i < arguments.size(); i++) {
            l.add(ts.unknownType(position()));
        }

        ConstructorInstance ci =
                ts.constructorInstance(position(),
                                       tb.currentClass(),
                                       Flags.NONE,
                                       l,
                                       Collections.<Type> emptyList());
        n = constructorInstance(n, ci);
        return n;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        ConstructorCall_c n = this;

        TypeSystem ts = tc.typeSystem();
        Context c = tc.context();

        ClassType ct = c.currentClass();
        Type superType = ct.superType();

        Expr qualifier = n.qualifier;
        Kind kind = n.kind;

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
                        + "constructor invocation.", position());
            }

            if (!superType.isClass() || !superType.toClass().isInnerClass()
                    || superType.toClass().inStaticContext()) {
                throw new SemanticException("The class \""
                                                    + superType
                                                    + "\""
                                                    + " is not an inner class, or was declared in a static "
                                                    + "context; a qualified constructor invocation cannot "
                                                    + "be used.",
                                            position());
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
                        + " is not a class.", position());
            }

            Expr q = qualifier;

            // If the super class is an inner class (i.e., has an enclosing
            // instance of its container class), then either a qualifier 
            // must be provided, or ct must have an enclosing instance of the
            // super class's container class, or a subclass thereof.
            // See JLS 2nd Ed. | 8.8.5.1.
            if (q == null && superType.isClass()
                    && superType.toClass().isInnerClass()
                    && !superType.toClass().inStaticContext()) {
                ClassType superContainer = superType.toClass().outer();
                // ct needs an enclosing instance of superContainer, 
                // or a subclass of superContainer.
                ClassType e = ct;

                while (e != null) {
                    if (e.isSubtype(superContainer)
                            && ct.hasEnclosingInstance(e)) {
                        NodeFactory nf = tc.nodeFactory();
                        // If an enclosing instance is of an anonymous class,
                        // there is no qualifier.
                        q =
                                e.isAnonymous()
                                        ? null
                                        : nf.This(position(),
                                                  nf.CanonicalTypeNode(position(),
                                                                       e))
                                            .type(e);
                        break;
                    }
                    e = e.outer();
                }

                if (e == null) {
                    throw new SemanticException(ct
                                                        + " must have an enclosing instance"
                                                        + " that is a subtype of "
                                                        + superContainer,
                                                position());
                }
                if (e == ct) {
                    throw new SemanticException(ct
                                                        + " is a subtype of "
                                                        + superContainer
                                                        + "; an enclosing instance that is a subtype of "
                                                        + superContainer
                                                        + " must be specified in the super constructor call.",
                                                position());
                }
            }

            n = qualifier(n, q);
        }

        List<Type> argTypes = new LinkedList<>();

        for (Expr e : n.arguments) {
            if (!e.isDisambiguated()) {
                return this;
            }
            argTypes.add(e.type());
        }

        if (kind == SUPER) {
            ct = ct.superType().toClass();
        }

        ConstructorInstance ci =
                ts.findConstructor(ct, argTypes, c.currentClass(), false);
        n = constructorInstance(n, ci);
        return n;
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == qualifier) {
            // FIXME: Can be more specific
            return ts.Object();
        }

        Iterator<Expr> i = this.arguments.iterator();
        Iterator<? extends Type> j = ci.formalTypes().iterator();

        while (i.hasNext() && j.hasNext()) {
            Expr e = i.next();
            Type t = j.next();

            if (e == child) {
                return t;
            }
        }

        return child.type();
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        ConstructorCall_c n = (ConstructorCall_c) super.extRewrite(rw);
        n = constructorInstance(n, null);
        return n;
    }

    @Override
    public String toString() {
        return (qualifier != null ? qualifier + "." : "") + kind + "(...)";
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (qualifier != null) {
            printSubExpr(qualifier, w, tr);
            w.write(".");
        }

        w.write(kind.toString());
        printArgs(w, tr);
        w.write(";");
    }

    @Override
    public void printArgs(CodeWriter w, PrettyPrinter tr) {
        w.write("(");
        w.allowBreak(2, 2, "", 0);
        w.begin(0);

        for (Iterator<Expr> i = arguments.iterator(); i.hasNext();) {
            Expr e = i.next();

            print(e, w, tr);

            if (i.hasNext()) {
                w.write(",");
                w.allowBreak(0);
            }
        }

        w.end();
        w.write(")");
    }

    @Override
    public Term firstChild() {
        if (qualifier != null) {
            return qualifier;
        }
        else {
            return listChild(arguments, (Expr) null);
        }
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (qualifier != null) {
            if (!arguments.isEmpty()) {
                v.visitCFG(qualifier, listChild(arguments, (Expr) null), ENTRY);
                v.visitCFGList(arguments, this, EXIT);
            }
            else {
                v.visitCFG(qualifier, this, EXIT);
            }
        }
        else {
            if (!arguments.isEmpty()) {
                v.visitCFGList(arguments, this, EXIT);
            }
        }

        return succs;
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        List<Type> l = new LinkedList<>();
        l.addAll(ci.throwTypes());
        l.addAll(ts.uncheckedExceptions());
        return l;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.ConstructorCall(this.position,
                                  this.kind,
                                  this.qualifier,
                                  this.arguments);
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

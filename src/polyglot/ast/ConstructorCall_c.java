/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
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
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A <code>ConstructorCall_c</code> represents a direct call to a constructor.
 * For instance, <code>super(...)</code> or <code>this(...)</code>.
 */
public class ConstructorCall_c extends Stmt_c implements ConstructorCall {
    protected Kind kind;
    protected Expr qualifier;
    protected List<Expr> arguments;
    protected ConstructorInstance ci;

    public ConstructorCall_c(Position pos, Kind kind, Expr qualifier,
            List<? extends Expr> arguments) {
        super(pos);
        assert (kind != null && arguments != null); // qualifier may be null
        this.kind = kind;
        this.qualifier = qualifier;
        this.arguments = ListUtil.copy(arguments, true);
    }

    /** Get the qualifier of the constructor call. */
    @Override
    public Expr qualifier() {
        return this.qualifier;
    }

    /** Set the qualifier of the constructor call. */
    @Override
    public ConstructorCall qualifier(Expr qualifier) {
        ConstructorCall_c n = (ConstructorCall_c) copy();
        n.qualifier = qualifier;
        return n;
    }

    /** Get the kind of the constructor call. */
    @Override
    public Kind kind() {
        return this.kind;
    }

    /** Set the kind of the constructor call. */
    @Override
    public ConstructorCall kind(Kind kind) {
        ConstructorCall_c n = (ConstructorCall_c) copy();
        n.kind = kind;
        return n;
    }

    /** Get the actual arguments of the constructor call. */
    @Override
    public List<Expr> arguments() {
        return Collections.unmodifiableList(this.arguments);
    }

    /** Set the actual arguments of the constructor call. */
    @Override
    public ProcedureCall arguments(List<Expr> arguments) {
        ConstructorCall_c n = (ConstructorCall_c) copy();
        n.arguments = ListUtil.copy(arguments, true);
        return n;
    }

    @Override
    public ProcedureInstance procedureInstance() {
        return constructorInstance();
    }

    /** Get the constructor we are calling. */
    @Override
    public ConstructorInstance constructorInstance() {
        return ci;
    }

    /** Set the constructor we are calling. */
    @Override
    public ConstructorCall constructorInstance(ConstructorInstance ci) {
        if (ci == this.ci) return this;
        ConstructorCall_c n = (ConstructorCall_c) copy();
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
    protected ConstructorCall_c reconstruct(Expr qualifier, List<Expr> arguments) {
        if (qualifier != this.qualifier
                || !CollectionUtil.equals(arguments, this.arguments)) {
            ConstructorCall_c n = (ConstructorCall_c) copy();
            n.qualifier = qualifier;
            n.arguments = ListUtil.copy(arguments, true);
            return n;
        }

        return this;
    }

    /** Visit the children of the call. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr qualifier = (Expr) visitChild(this.qualifier, v);
        List<Expr> arguments = visitList(this.arguments, v);
        return reconstruct(qualifier, arguments);
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

        List<Type> l = new ArrayList<Type>(arguments.size());
        for (int i = 0; i < arguments.size(); i++) {
            l.add(ts.unknownType(position()));
        }

        ConstructorInstance ci =
                ts.constructorInstance(position(),
                                       tb.currentClass(),
                                       Flags.NONE,
                                       l,
                                       Collections.<Type> emptyList());
        return n.constructorInstance(ci);
    }

    /** Type check the call. */
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
            if (q == null && superType.isClass()
                    && superType.toClass().isInnerClass()) {
                ClassType superContainer = superType.toClass().outer();
                // ct needs an enclosing instance of superContainer, 
                // or a subclass of superContainer.
                ClassType e = ct;

                while (e != null) {
                    if (e.isSubtype(superContainer)
                            && ct.hasEnclosingInstance(e)) {
                        NodeFactory nf = tc.nodeFactory();
                        q =
                                nf.This(position(),
                                        nf.CanonicalTypeNode(position(), e))
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

            if (qualifier != q) n = (ConstructorCall_c) n.qualifier(q);
        }

        List<Type> argTypes = new LinkedList<Type>();

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
                ts.findConstructor(ct, argTypes, c.currentClass());
        return n.constructorInstance(ci);
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
    public String toString() {
        return (qualifier != null ? qualifier + "." : "") + kind + "(...)";
    }

    /** Write the call to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (qualifier != null) {
            print(qualifier, w, tr);
            w.write(".");
        }

        w.write(kind + "(");

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

        w.write(");");
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
        List<Type> l = new LinkedList<Type>();
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

}

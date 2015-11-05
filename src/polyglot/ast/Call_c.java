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
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ProcedureInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A {@code Call} is an immutable representation of a Java
 * method call.  It consists of a method name and a list of arguments.
 * It may also have either a Type upon which the method is being
 * called or an expression upon which the method is being called.
 */
public class Call_c extends Expr_c implements Call, CallOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Receiver target;
    protected Id name;
    protected List<Expr> arguments;
    protected MethodInstance mi;
    protected boolean targetImplicit;

//    @Deprecated
    public Call_c(Position pos, Receiver target, Id name,
            List<Expr> arguments) {
        this(pos, target, name, arguments, null);
    }

    public Call_c(Position pos, Receiver target, Id name, List<Expr> arguments,
            Ext ext) {
        super(pos, ext);
        assert name != null && arguments != null; // target may be null
        this.target = target;
        this.name = name;
        this.arguments = ListUtil.copy(arguments, true);
        targetImplicit = target == null;
    }

    @Override
    public Precedence precedence() {
        return Precedence.LITERAL;
    }

    @Override
    public Receiver target() {
        return target;
    }

    @Override
    public Call target(Receiver target) {
        return target(this, target);
    }

    protected <N extends Call_c> N target(N n, Receiver target) {
        if (n.target == target) return n;
        n = copyIfNeeded(n);
        n.target = target;
        return n;
    }

    @Override
    public Id id() {
        return name;
    }

    @Override
    public Call id(Id name) {
        return id(this, name);
    }

    protected <N extends Call_c> N id(N n, Id name) {
        if (n.name == name) return n;
        n = copyIfNeeded(n);
        n.name = name;
        return n;
    }

    @Override
    public String name() {
        return name.id();
    }

    @Override
    public Call name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public ProcedureInstance procedureInstance() {
        return methodInstance();
    }

    @Override
    public MethodInstance methodInstance() {
        return mi;
    }

    @Override
    public Call methodInstance(MethodInstance mi) {
        return methodInstance(this, mi);
    }

    protected <N extends Call_c> N methodInstance(N n, MethodInstance mi) {
        if (n.mi == mi) return n;
        n = copyIfNeeded(n);
        n.mi = mi;
        return n;
    }

    @Override
    public boolean isTargetImplicit() {
        return targetImplicit;
    }

    @Override
    public Call targetImplicit(boolean targetImplicit) {
        return targetImplicit(this, targetImplicit);
    }

    protected <N extends Call_c> N targetImplicit(N n, boolean targetImplicit) {
        if (n.targetImplicit == targetImplicit) return n;
        n = copyIfNeeded(n);
        n.targetImplicit = targetImplicit;
        return n;
    }

    @Override
    public List<Expr> arguments() {
        return arguments;
    }

    @Override
    public ProcedureCall arguments(List<Expr> arguments) {
        return arguments(this, arguments);
    }

    protected <N extends Call_c> N arguments(N n, List<Expr> arguments) {
        if (CollectionUtil.equals(n.arguments, arguments)) return n;
        n = copyIfNeeded(n);
        n.arguments = ListUtil.copy(arguments, true);
        return n;
    }

    /** Reconstruct the call. */
    protected <N extends Call_c> N reconstruct(N n, Receiver target, Id name,
            List<Expr> arguments) {
        n = target(n, target);
        n = id(n, name);
        n = arguments(n, arguments);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Receiver target = visitChild(this.target, v);
        Id name = visitChild(this.name, v);
        List<Expr> arguments = visitList(this.arguments, v);
        return reconstruct(this, target, name, arguments);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        Call_c n = (Call_c) super.buildTypes(tb);

        TypeSystem ts = tb.typeSystem();

        List<Type> l = new ArrayList<>(arguments.size());
        for (int i = 0; i < arguments.size(); i++) {
            l.add(ts.unknownType(position()));
        }

        MethodInstance mi =
                ts.methodInstance(position(),
                                  tb.currentClass(),
                                  Flags.NONE,
                                  ts.unknownType(position()),
                                  name.id(),
                                  l,
                                  Collections.<Type> emptyList());
        return methodInstance(n, mi);
    }

    @Override
    public Node typeCheckNullTarget(TypeChecker tc, List<Type> argTypes)
            throws SemanticException {
        TypeSystem ts = tc.typeSystem();
        NodeFactory nf = tc.nodeFactory();
        Context c = tc.context();

        // the target is null, and thus implicit
        // let's find the target, using the context, and
        // set the target appropriately, and then type check
        // the result
        MethodInstance mi = c.findMethod(name.id(), argTypes);

        Receiver r;
        if (mi.flags().isStatic()) {
            Type container = tc.lang().findContainer(this, ts, mi);
            r = nf.CanonicalTypeNode(position().startOf(), container)
                  .type(container);
        }
        else {
            // The method is non-static, so we must prepend with "this", but we
            // need to determine if the "this" should be qualified.  Get the
            // enclosing class which brought the method into scope.  This is
            // different from mi.container().  mi.container() returns a super type
            // of the class we want.
            ClassType scope = c.findMethodScope(name.id());

            if (!ts.equals(scope, c.currentClass())) {
                r = nf.This(position().startOf(),
                            nf.CanonicalTypeNode(position().startOf(), scope))
                      .type(scope);
            }
            else {
                r = nf.This(position().startOf()).type(scope);
            }
        }

        // we call computeTypes on the receiver too.
        Call_c call = this;
        call = targetImplicit(call, true);
        call = target(call, r);
        return call.visit(tc.rethrowMissingDependencies(true));
    }

    @Override
    public Type findContainer(TypeSystem ts, MethodInstance mi) {
        return ts.staticTarget(mi.container());
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();
        Context c = tc.context();

        List<Type> argTypes = new ArrayList<>(arguments.size());

        for (Expr e : arguments) {
            if (!e.type().isCanonical()) {
                return this;
            }
            argTypes.add(e.type());
        }

        if (target == null) {
            return tc.lang().typeCheckNullTarget(this, tc, argTypes);
        }

        if (!target.type().isCanonical()) {
            return this;
        }

        ReferenceType targetType = tc.lang().findTargetType(this);
        MethodInstance mi =
                ts.findMethod(targetType,
                              name.id(),
                              argTypes,
                              c.currentClass(),
                              !(target instanceof Special));

        /* This call is in a static context if and only if
         * the target (possibly implicit) is a type node.
         */
        boolean staticContext = target instanceof TypeNode;

        if (staticContext && !mi.flags().isStatic()) {
            throw new SemanticException("Cannot call non-static method "
                    + name.id() + " of " + target.type() + " in static "
                    + "context.", this.position());
        }

        // If the target is super, but the method is abstract, then complain.
        if (target instanceof Special
                && ((Special) target).kind() == Special.SUPER
                && mi.flags().isAbstract()) {
            throw new SemanticException("Cannot call an abstract method "
                    + "of the super class", this.position());
        }

        Call_c call = this;
        call = methodInstance(call, mi);
        call = type(call, mi.returnType());
        return call;
    }

    @Override
    public ReferenceType findTargetType() throws SemanticException {
        Type t = target.type();
        if (t.isReference()) {
            return t.toReference();
        }
        else {
            // trying to invoke a method on a non-reference type.
            // let's pull out an appropriate error message.
            if (target instanceof Expr) {
                throw new SemanticException("Cannot invoke method \"" + name
                        + "\" on " + "an expression of non-reference type " + t
                        + ".", target.position());
            }
            else if (target instanceof TypeNode) {
                throw new SemanticException("Cannot invoke static method \""
                        + name + "\" on non-reference type " + t + ".",
                                            target.position());
            }
            throw new SemanticException("Cannot invoke method \"" + name
                    + "\" on non-reference type " + t + ".", target.position());
        }
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        if (child == target) {
            return mi.container();
        }

        Iterator<Expr> i = arguments.iterator();
        Iterator<? extends Type> j = mi.formalTypes().iterator();

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
        StringBuffer sb = new StringBuffer();
        sb.append(targetImplicit ? "" : target.toString() + ".");
        sb.append(name);
        sb.append("(");

        int count = 0;

        for (Iterator<Expr> i = arguments.iterator(); i.hasNext();) {
            if (count++ > 2) {
                sb.append("...");
                break;
            }

            Expr n = i.next();
            sb.append(n.toString());

            if (i.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append(")");
        return sb.toString();
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (!targetImplicit) {
            if (target instanceof Expr) {
                printSubExpr((Expr) target, w, tr);
            }
            else if (target != null) {
                print(target, w, tr);
            }
            w.write(".");
            w.allowBreak(2, 3, "", 0);
        }

        w.begin(0);
        w.write(name.id());
        printArgs(w, tr);
        w.end();
    }

    @Override
    public void printArgs(CodeWriter w, PrettyPrinter tr) {
        w.write("(");
        w.allowBreak(2, 2, "", 0);
        w.begin(0);

        for (Iterator<Expr> i = arguments.iterator(); i.hasNext();) {
            Expr e = i.next();

            w.begin(2);
            print(e, w, tr);
            w.end();

            if (i.hasNext()) {
                w.write(",");
                w.allowBreak(0);
            }
        }

        w.end();
        w.write(")");
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(targetImplicit " + targetImplicit + ")");
        w.end();

        if (mi != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(instance " + mi + ")");
            w.end();
        }

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(name " + name + ")");
        w.end();

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(arguments " + arguments + ")");
        w.end();
    }

    @Override
    public Term firstChild() {
        if (target instanceof Term) {
            return (Term) target;
        }
        return listChild(arguments, (Expr) null);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (target instanceof Term) {
            Term t = (Term) target;

            if (!arguments.isEmpty()) {
                v.visitCFG(t, listChild(arguments, (Expr) null), ENTRY);
                v.visitCFGList(arguments, this, EXIT);
            }
            else {
                v.visitCFG(t, this, EXIT);
            }
        }
        else {
            v.visitCFGList(arguments, this, EXIT);
        }

        return succs;
    }

    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        if (mi == null) {
            throw new InternalCompilerError(position(),
                                            "Null method instance after type "
                                                    + "check.");
        }

        return super.exceptionCheck(ec);
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        List<Type> l = new LinkedList<>();

        l.addAll(mi.throwTypes());
        l.addAll(ts.uncheckedExceptions());

        if (target instanceof Expr && !(target instanceof Special)) {
            l.add(ts.NullPointerException());
        }

        return l;
    }

    @Override
    public NodeVisitor extRewriteEnter(ExtensionRewriter rw)
            throws SemanticException {
        if (isTargetImplicit()) {
            // don't translate the target
            return rw.bypass(target());
        }
        return super.extRewriteEnter(rw);
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        Call_c c = (Call_c) super.extRewrite(rw);
        c = methodInstance(c, null);
        if (isTargetImplicit()) {
            c = target(c, null);
        }
        return c;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Call(position, target, name, arguments);
    }

}

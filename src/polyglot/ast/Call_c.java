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
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A <code>Call</code> is an immutable representation of a Java
 * method call.  It consists of a method name and a list of arguments.
 * It may also have either a Type upon which the method is being
 * called or an expression upon which the method is being called.
 */
public class Call_c extends Expr_c implements Call {
    protected Receiver target;
    protected Id name;
    protected List<Expr> arguments;
    protected MethodInstance mi;
    protected boolean targetImplicit;

    public Call_c(Position pos, Receiver target, Id name, List<Expr> arguments) {
        super(pos);
        assert (name != null && arguments != null); // target may be null
        this.target = target;
        this.name = name;
        this.arguments = ListUtil.copy(arguments, true);
        this.targetImplicit = (target == null);
    }

    /** Get the precedence of the call. */
    @Override
    public Precedence precedence() {
        return Precedence.LITERAL;
    }

    /** Get the target object or type of the call. */
    @Override
    public Receiver target() {
        return this.target;
    }

    /** Set the target object or type of the call. */
    @Override
    public Call target(Receiver target) {
        Call_c n = (Call_c) copy();
        n.target = target;
        return n;
    }

    /** Get the name of the call. */
    @Override
    public Id id() {
        return this.name;
    }

    /** Set the name of the call. */
    @Override
    public Call id(Id name) {
        Call_c n = (Call_c) copy();
        n.name = name;
        return n;
    }

    /** Get the name of the call. */
    @Override
    public String name() {
        return this.name.id();
    }

    /** Set the name of the call. */
    @Override
    public Call name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public ProcedureInstance procedureInstance() {
        return methodInstance();
    }

    /** Get the method instance of the call. */
    @Override
    public MethodInstance methodInstance() {
        return this.mi;
    }

    /** Set the method instance of the call. */
    @Override
    public Call methodInstance(MethodInstance mi) {
        if (mi == this.mi) return this;
        Call_c n = (Call_c) copy();
        n.mi = mi;
        return n;
    }

    @Override
    public boolean isTargetImplicit() {
        return this.targetImplicit;
    }

    @Override
    public Call targetImplicit(boolean targetImplicit) {
        if (targetImplicit == this.targetImplicit) {
            return this;
        }

        Call_c n = (Call_c) copy();
        n.targetImplicit = targetImplicit;
        return n;
    }

    /** Get the actual arguments of the call. */
    @Override
    public List<Expr> arguments() {
        return this.arguments;
    }

    /** Set the actual arguments of the call. */
    @Override
    public ProcedureCall arguments(List<Expr> arguments) {
        Call_c n = (Call_c) copy();
        n.arguments = ListUtil.copy(arguments, true);
        return n;
    }

    /** Reconstruct the call. */
    protected Call_c reconstruct(Receiver target, Id name, List<Expr> arguments) {
        if (target != this.target || name != this.name
                || !CollectionUtil.equals(arguments, this.arguments)) {
            Call_c n = (Call_c) copy();

            // If the target changes, assume we want it to be an explicit target.
            n.targetImplicit &= target == n.target;

            n.target = target;
            n.name = name;
            n.arguments = ListUtil.copy(arguments, true);
            return n;
        }

        return this;
    }

    /** Visit the children of the call. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Receiver target = (Receiver) visitChild(this.target, v);
        Id name = (Id) visitChild(this.name, v);
        List<Expr> arguments = visitList(this.arguments, v);
        return reconstruct(target, name, arguments);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        Call_c n = (Call_c) super.buildTypes(tb);

        TypeSystem ts = tb.typeSystem();

        List<Type> l = new ArrayList<Type>(arguments.size());
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
        return n.methodInstance(mi);
    }

    /**
     * Typecheck the Call when the target is null. This method finds
     * an appropriate target, and then type checks accordingly.
     * 
     * @param argTypes list of <code>Type</code>s of the arguments
     */
    protected Node typeCheckNullTarget(TypeChecker tc, List<Type> argTypes)
            throws SemanticException {
        TypeSystem ts = tc.typeSystem();
        NodeFactory nf = tc.nodeFactory();
        Context c = tc.context();

        // the target is null, and thus implicit
        // let's find the target, using the context, and
        // set the target appropriately, and then type check
        // the result
        MethodInstance mi = c.findMethod(this.name.id(), argTypes);

        Receiver r;
        if (mi.flags().isStatic()) {
            Type container = findContainer(ts, mi);
            r =
                    nf.CanonicalTypeNode(position().startOf(), container)
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
                r =
                        nf.This(position().startOf(),
                                nf.CanonicalTypeNode(position().startOf(),
                                                     scope)).type(scope);
            }
            else {
                r = nf.This(position().startOf()).type(scope);
            }
        }

        // we call computeTypes on the reciever too.
        Call_c call = (Call_c) this.targetImplicit(true).target(r);
        return call.visit(tc.rethrowMissingDependencies(true));
    }

    /**
     * Used to find the missing static target of a static method call.
     * Should return the container of the method instance. 
     * 
     */
    protected Type findContainer(TypeSystem ts, MethodInstance mi) {
        return ts.staticTarget(mi.container());
    }

    /** Type check the call. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();
        Context c = tc.context();

        List<Type> argTypes = new ArrayList<Type>(this.arguments.size());

        for (Expr e : arguments) {
            if (!e.type().isCanonical()) {
                return this;
            }
            argTypes.add(e.type());
        }

        if (this.target == null) {
            return this.typeCheckNullTarget(tc, argTypes);
        }

        if (!this.target.type().isCanonical()) {
            return this;
        }

        ReferenceType targetType = this.findTargetType();
        MethodInstance mi =
                ts.findMethod(targetType,
                              this.name.id(),
                              argTypes,
                              c.currentClass());

        /* This call is in a static context if and only if
         * the target (possibly implicit) is a type node.
         */
        boolean staticContext = (this.target instanceof TypeNode);

        if (staticContext && !mi.flags().isStatic()) {
            throw new SemanticException("Cannot call non-static method "
                    + this.name.id() + " of " + target.type() + " in static "
                    + "context.", this.position());
        }

        // If the target is super, but the method is abstract, then complain.
        if (this.target instanceof Special
                && ((Special) this.target).kind() == Special.SUPER
                && mi.flags().isAbstract()) {
            throw new SemanticException("Cannot call an abstract method "
                    + "of the super class", this.position());
        }

        Call_c call = (Call_c) this.methodInstance(mi).type(mi.returnType());

        // If we found a method, the call must type check, so no need to check
        // the arguments here.
        call.checkConsistency(c);

        return call;
    }

    protected ReferenceType findTargetType() throws SemanticException {
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
                                                    + name
                                                    + "\" on non-reference type "
                                                    + t + ".",
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

        Iterator<Expr> i = this.arguments.iterator();
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

    /** Write the expression to an output file. */
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
        w.write(name + "(");
        if (arguments.size() > 0) {
            w.allowBreak(2, 2, "", 0); // miser mode
            w.begin(0);

            for (Iterator<Expr> i = arguments.iterator(); i.hasNext();) {
                Expr e = i.next();
                print(e, w, tr);

                if (i.hasNext()) {
                    w.write(",");
                    w.allowBreak(0, " ");
                }
            }

            w.end();
        }
        w.write(")");
        w.end();
    }

    /** Dumps the AST. */
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

    /** Check exceptions thrown by the call. */
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
        List<Type> l = new LinkedList<Type>();

        l.addAll(mi.throwTypes());
        l.addAll(ts.uncheckedExceptions());

        if (target instanceof Expr && !(target instanceof Special)) {
            l.add(ts.NullPointerException());
        }

        return l;
    }

    // check that the implicit target setting is correct.
    protected void checkConsistency(Context c) throws SemanticException {
        if (targetImplicit) {
            // the target is implicit. Check that the
            // method found in the target type is the
            // same as the method found in the context.

            // as exception will be thrown if no appropriate method
            // exists. 
            @SuppressWarnings("unused")
            MethodInstance ctxtMI = c.findMethod(name.id(), mi.formalTypes());

            // cannot perform this check due to the context's findMethod returning a 
            // different method instance than the typeSystem in some situations
//          if (!c.typeSystem().equals(ctxtMI, mi)) {
//              throw new InternalCompilerError("Method call " + this + " has an " +
//                   "implicit target, but the name " + name + " resolves to " +
//                   ctxtMI + " in " + ctxtMI.container() + " instead of " + mi+ " in " + mi.container(), position());
//          }
        }
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Call(this.position, this.target, this.name, this.arguments);
    }

}

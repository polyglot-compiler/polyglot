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
package polyglot.ext.jl5.visit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.Block;
import polyglot.ast.Call;
import polyglot.ast.Cast;
import polyglot.ast.ClassBody;
import polyglot.ast.Expr;
import polyglot.ast.Formal;
import polyglot.ast.Local;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Special;
import polyglot.ast.Stmt;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.JL5Options;
import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.ext.jl5.types.JL5LocalInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5ProcedureInstance;
import polyglot.ext.jl5.types.JL5Subst;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.frontend.Job;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.ErrorHandlingVisitor;
import polyglot.visit.NodeVisitor;

/**
 * This class rewrites method decls to change the type of arguments and the return value
 * so that the appropriate override relationships will hold in Java 1.4. Also adds in 
 * new MethodDecls to match the erased signature of any overridden methods.
 */
public class TypeErasureProcDecls extends ErrorHandlingVisitor {
    private List<MethodDecl> newMethodDecls;

    public TypeErasureProcDecls(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
        this.newMethodDecls = new ArrayList<>();
    }

    @Override
    protected NodeVisitor enterCall(Node n) throws SemanticException {
        if (n instanceof ClassBody) {
            // push a new visitor, so that the newMethodDecls end up in the right place.
            return new TypeErasureProcDecls(job, ts, nf);
        }
        return super.enterCall(n);
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        if (n instanceof MethodDecl) {
            return rewriteMethodDecl((MethodDecl) n);
        }
        if (n instanceof ClassBody) {
            ClassBody cb = (ClassBody) n;
            List<MethodDecl> nmd = ((TypeErasureProcDecls) v).newMethodDecls;

            for (MethodDecl md : nmd) {
                cb = cb.addMember(md);
            }
            nmd.clear();
            return cb;
        }
        return super.leaveCall(n);
    }

    private Node rewriteMethodDecl(MethodDecl n) {
        // find the instance that it overrides
        MethodInstance mi = n.methodInstance();
        JL5TypeSystem ts = (JL5TypeSystem) this.typeSystem();

        List<? extends MethodInstance> implemented = mi.implemented();
        if (implemented.isEmpty()) {
            // doesn't implement anything
            return n;
        }

        // get the last most element, ideally from a class...
        MethodInstance mj = null;
        for (int i = implemented.size() - 1; i >= 0; i--) {
            MethodInstance mk = implemented.get(i);
            if (mk == mi) {
                // don't bother if mk is the same as mi
                continue;
            }
            else if (mj == null) {
                // best so far!
                mj = mk;
            }
            else if (mk.container().isClass()
                    && !mk.container().toClass().flags().isInterface()) {
                // we found a class Let's prefer that, unless mk overrides mj.
                if (!ts.implemented(mk).contains(mj)) {
                    mj = mk;
                }
                break;
            }
        }
        if (mj == null) {
            // doesn't implement anything
            return n;
        }

        JL5ClassType miContainer = (JL5ClassType) mi.container();
        List<? extends Type> miFormalTypes = mi.formalTypes();
        if (miContainer instanceof JL5ParsedClassType) {
            JL5ParsedClassType pct = (JL5ParsedClassType) miContainer;
            JL5Subst es = pct.erasureSubst();
            if (es != null) {
                miFormalTypes = es.substTypeList(miFormalTypes);
            }
        }
        MethodInstance mjErased = erasedMethodInstance(mj);

        // we need to rewrite the method decl to have the same arguments as mjErased, the erased version of mj.
        List<? extends Type> mjErasedFormals = erase(mjErased.formalTypes());
        boolean changed = false;
        List<Formal> newFormals = new ArrayList<>(n.formals().size());
        Iterator<Formal> formals = n.formals().iterator();
        for (Type tj : mjErasedFormals) {
            Formal f = formals.next();
            TypeNode tn = f.type();
            TypeNode newTn = tn.type(ts.erasureType(tj));
            changed |= (tn != newTn);
            newFormals.add(f.type(newTn));
        }

        TypeNode newRetType = n.returnType();
        newRetType =
                newRetType.type(erasedReturnType(mjErased, n.returnType()
                                                            .type()));
        changed |= (n.returnType().type() != newRetType.type());

        // if we are not an interface, then add a method decl for each overridden method.
        if (n.methodInstance().container().isClass()
                && !n.methodInstance()
                     .container()
                     .toClass()
                     .flags()
                     .isInterface()) {
            Set<List<? extends Type>> alreadyAddedMethods =
                    new LinkedHashSet<>();
            alreadyAddedMethods.add(mjErasedFormals);

            // now we need to go through all the other methods we override, 
            // and check to see if we 
            // need to add a new method for it...
            for (MethodInstance mk : implemented) {
                // do we have a method that overrides mk correctly?
                MethodInstance mkErased = erasedMethodInstance(mk);
                List<? extends Type> mkErasedFormals =
                        erase(mkErased.formalTypes());

                if (alreadyAddedMethods.contains(mkErasedFormals)) {
                    // we already have a method that erases to the required signature
                    continue;
                }
                alreadyAddedMethods.add(mkErasedFormals);

                // we need to add a method declaration for mkErased
                addMethodDecl(mkErased,
                              mk.returnType(),
                              mjErased,
                              n,
                              mjErasedFormals);

            }
        }
        if (!changed) {
            return n;
        }
        return n.returnType(newRetType).formals(newFormals);
    }

    protected Type erasedReturnType(MethodInstance mjErased,
            Type originalReturnType) {
        Type t = originalReturnType;
        JL5Options opts = (JL5Options) ts.extensionInfo().getOptions();
        if (!opts.leaveCovariantReturns) {
            // also change the return type, so Java 1.4 won't complain
            t = mjErased.returnType();
        }
        return t;
    }

    protected void addMethodDecl(MethodInstance mkErased, Type origReturnType,
            MethodInstance callee, MethodDecl n,
            List<? extends Type> dispatchArgTypes) {
        Position pos = Position.compilerGenerated();
        Flags flags = n.flags();
        if (flags.isAbstract()
                && n.memberInstance().container().isClass()
                && !n.memberInstance()
                     .container()
                     .toClass()
                     .flags()
                     .isInterface()) {
            // n is abstract, but it is not in an interface, so lets add a method body to dispatch correctly.
            flags = flags.clearAbstract();
        }
        TypeNode returnType =
                nodeFactory().CanonicalTypeNode(pos,
                                                erasedReturnType(mkErased,
                                                                 origReturnType));

        List<? extends Type> mkErasedFormals = erase(mkErased.formalTypes());
        List<Formal> formals = new ArrayList<>(mkErased.formalTypes().size());
        int i = 0;
        for (Type ft : mkErasedFormals) {
            Formal f =
                    nodeFactory().Formal(pos,
                                         Flags.NONE,
                                         nodeFactory().CanonicalTypeNode(pos,
                                                                         ft),
                                         nodeFactory().Id(pos, "arg" + (++i)));
            JL5LocalInstance li =
                    (JL5LocalInstance) ts.localInstance(pos,
                                                        Flags.NONE,
                                                        ft,
                                                        "arg" + i);
            li.setProcedureFormal(true);
            f = f.localInstance(li);
            formals.add(f);
        }

        List<TypeNode> throwTypes =
                new ArrayList<>(mkErased.throwTypes().size());
        for (Type tt : mkErased.throwTypes()) {
            throwTypes.add(nodeFactory().CanonicalTypeNode(pos, tt));
        }

        List<Expr> actualArgs = new ArrayList<>(mkErasedFormals.size());
        i = 0;
        for (Type dt : dispatchArgTypes) {
            LocalInstance li = formals.get(i).localInstance();
            Local l =
                    nodeFactory().Local(pos,
                                        nodeFactory().Id(pos, "arg" + (++i)));

            l = l.localInstance(li);
            l = (Local) l.type(li.type());

            Expr arg = l;
            if (!li.type().isPrimitive()) {
                Cast cst =
                        nodeFactory().Cast(pos,
                                           nodeFactory().CanonicalTypeNode(pos,
                                                                           dt),
                                           l);
                cst = (Cast) cst.type(dt);
                arg = cst;
            }
            actualArgs.add(arg);
        }
        Block body = null;
        if (!flags.isAbstract()) {
            // we need a body that dispatches to another method
            Special spec =
                    (Special) nodeFactory().Special(pos, Special.THIS)
                                           .type(callee.container());

            Call c =
                    nodeFactory().Call(pos,
                                       spec,
                                       nodeFactory().Id(pos, mkErased.name()),
                                       actualArgs);
            c = c.methodInstance(callee);
            c = (Call) c.type(callee.returnType());

            Stmt s;
            if (origReturnType.isVoid()) {
                s = nodeFactory().Eval(pos, c);
            }
            else {
                Cast cast = nodeFactory().Cast(pos, returnType, c);
                cast = (Cast) cast.type(returnType.type());
                s = nodeFactory().Return(pos, cast);
            }
            body = nodeFactory().Block(pos, s);
        }
        MethodDecl newMd =
                nodeFactory().MethodDecl(pos,
                                         flags,
                                         returnType,
                                         nodeFactory().Id(pos, mkErased.name()),
                                         formals,
                                         throwTypes,
                                         body);
        newMd =
                newMd.methodInstance(typeSystem().methodInstance(pos,
                                                                 callee.container(),
                                                                 flags,
                                                                 returnType.type(),
                                                                 callee.name(),
                                                                 mkErasedFormals,
                                                                 mkErased.throwTypes()));
        this.newMethodDecls.add(newMd);
    }

    private List<? extends Type> erase(List<? extends Type> types) {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        List<Type> nt = new ArrayList<>(types.size());
        for (Type t : types) {
            nt.add(ts.erasureType(t));
        }
        return nt;
    }

    /**
     * Get an erased version of method instance mj.
     * @param mj
     * @return
     */
    protected MethodInstance erasedMethodInstance(MethodInstance mj) {
        // we'll get the erasure substitutions for mj, and for mj.container(),
        // and combine them, then erase mj.
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        Map<TypeVariable, ReferenceType> subst = new HashMap<>();

        JL5ParsedClassType containerBase =
                (JL5ParsedClassType) ((JL5ClassType) mj.container()).declaration();
        JL5Subst containerSubst = ts.erasureSubst(containerBase);
        if (containerSubst != null) {
            subst.putAll(containerSubst.substitutions());
        }
        JL5Subst procedureSubst = ts.erasureSubst((JL5ProcedureInstance) mj);
        if (procedureSubst != null) {
            subst.putAll(procedureSubst.substitutions());
        }

        if (subst.isEmpty()) {
            return mj;
        }
        return ts.subst(subst).substMethod((MethodInstance) mj.declaration());
    }
}

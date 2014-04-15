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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.Assign;
import polyglot.ast.Call;
import polyglot.ast.CallOps;
import polyglot.ast.Expr;
import polyglot.ast.FieldDecl;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.Node_c;
import polyglot.ast.Return;
import polyglot.ast.Special;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.types.CodeInstance;
import polyglot.types.Context;
import polyglot.types.FunctionInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5CallExt extends JL5Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<TypeNode> typeArgs;

    public List<TypeNode> typeArgs() {
        return this.typeArgs;
    }

    public Call typeArgs(List<TypeNode> typeArgs) {
        if (this.typeArgs == typeArgs) {
            return (Call) this.node();
        }
        Call n = (Call) this.node().copy();
        JL5CallExt ext = (JL5CallExt) JL5Ext.ext(n);
        ext.typeArgs = typeArgs;
        return n;
    }

    private transient Type expectedReturnType = null;

    protected Type expectedReturnType() {
        return this.expectedReturnType;
    }

    protected void setExpectedReturnType(Type type) {
        if (type == null || !type.isCanonical()) {
            expectedReturnType = null;
            return;
        }
        expectedReturnType = type;
    }

    public Node visitChildren(NodeVisitor v) {
        JL5CallExt ext = (JL5CallExt) JL5Ext.ext(this.node());

        List<TypeNode> typeArgs = this.node().visitList(ext.typeArgs(), v);

        Node newN = this.node().visitChildren(v);
        JL5CallExt newext = (JL5CallExt) JL5Ext.ext(newN);

        if (!CollectionUtil.equals(typeArgs, newext.typeArgs())) {
            // the type args changed! Let's update the node.
            if (newN == this.node()) {
                // we need to create a copy.
                newN = (Node) newN.copy();
                newext = (JL5CallExt) JL5Ext.ext(newN);
            }
            else {
                // the call to super.visitChildren(v) already
                // created a copy of the node (and thus of its extension).
            }
            newext.typeArgs = typeArgs;
        }
        return newN;
    }

    public Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException {
        JL5CallExt ext = (JL5CallExt) JL5Ext.ext(this.node());
        if (parent instanceof Return) {
            CodeInstance ci = tc.context().currentCode();
            if (ci instanceof FunctionInstance) {
                ext.setExpectedReturnType(((FunctionInstance) ci).returnType());
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
                ext.setExpectedReturnType(type);
            }
        }
        if (parent instanceof LocalDecl) {
            LocalDecl ld = (LocalDecl) parent;
            Type type = ld.type().type();
            if (type == null || !type.isCanonical()) {
                // not ready yet
                return this.node();
            }
            ext.setExpectedReturnType(type);
        }
        if (parent instanceof FieldDecl) {
            FieldDecl fd = (FieldDecl) parent;
            Type type = fd.type().type();
            if (type == null || !type.isCanonical()) {
                // not ready yet
                return this.node();
            }
            ext.setExpectedReturnType(type);
        }

        return null;
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        Context c = tc.context();

        Call n = (Call) this.node();
        JL5CallExt ext = (JL5CallExt) JL5Ext.ext(n);

        List<Type> argTypes = new ArrayList<Type>(n.arguments().size());

        for (Expr e : n.arguments()) {
            if (!e.type().isCanonical()) {
                return n;
            }
            argTypes.add(e.type());
        }

        if (n.target() == null) {
            return ((CallOps) n.del()).typeCheckNullTarget(tc, argTypes);
        }

        if (!n.target().type().isCanonical()) {
            return n;
        }
        List<ReferenceType> actualTypeArgs =
                new ArrayList<ReferenceType>(ext.typeArgs().size());
        for (TypeNode tn : ext.typeArgs()) {
            actualTypeArgs.add((ReferenceType) tn.type());
        }

        ReferenceType targetType = ((CallOps) n.del()).findTargetType();

        /* This call is in a static context if and only if
         * the target (possibly implicit) is a type node.
         */
        boolean staticContext = (n.target() instanceof TypeNode);

        if (staticContext && targetType instanceof RawClass) {
            targetType = ((RawClass) targetType).base();
        }

        JL5MethodInstance mi =
                (JL5MethodInstance) ts.findMethod(targetType,
                                                  n.name(),
                                                  argTypes,
                                                  actualTypeArgs,
                                                  c.currentClass(),
                                                  ext.expectedReturnType());

//        System.err.println("\nJL5Call_c.typeCheck targettype is " + targetType);
//        System.err.println("  JL5Call_c.typeCheck target is " + this.target);
//        System.err.println("  JL5Call_c.typeCheck target type is "
//                + this.target.type());
//        if (this.target.type().isClass()) {
//            System.err.println("  JL5Call_c.typeCheck target type super is "
//                    + this.target.type().toClass().superType());
//        }
//        System.err.println("  JL5Call_c.expectedReturnType is "
//                + this.expectedReturnType);
//        System.err.println("  JL5Call_c.typeCheck arg types is " + argTypes);
//        System.err.println("  JL5Call_c.typeCheck mi is " + mi
//                + " return type is " + mi.returnType().getClass());
//        System.err.println("  JL5Call_c.typeCheck mi is " + mi
//                + " container is " + mi.container().getClass());
        if (staticContext && !mi.flags().isStatic()) {
            throw new SemanticException("Cannot call non-static method "
                    + n.name() + " of " + n.target().type() + " in static "
                    + "context.", n.position());
        }

        // If the target is super, but the method is abstract, then complain.
        if (n.target() instanceof Special
                && ((Special) n.target()).kind() == Special.SUPER
                && mi.flags().isAbstract()) {
            throw new SemanticException("Cannot call an abstract method "
                    + "of the super class", n.position());
        }

        Type returnType = computeReturnType(mi);

        n = (Call) n.methodInstance(mi).type(returnType);
        ext = (JL5CallExt) JL5Ext.ext(n);

        // Need to deal with Object.getClass() specially. See JLS 3rd ed., section 4.3.2
        if (mi.name().equals("getClass") && mi.container().equals(ts.Object())) {
            // the return type of the call is "Class<? extends |T|>" where T is the static type of
            // the receiver.
            Type t = n.target().type();
            ReferenceType et = (ReferenceType) ts.erasureType(t);
            ReferenceType wt = ts.wildCardType(n.position(), et, null);
            Type instClass =
                    ts.instantiate(n.position(),
                                   (JL5ParsedClassType) ts.Class(),
                                   Collections.singletonList(wt));
            n = (Call) n.type(instClass);
        }
        //        System.err.println("JL5Call_c: " + this + " got mi " + mi);

        return n;
    }

    protected Type computeReturnType(JL5MethodInstance mi)
            throws SemanticException {
        // See JLS 3rd ed 15.12.2.6
        JL5TypeSystem ts = (JL5TypeSystem) mi.typeSystem();
        // If the method being invoked is declared with a return type of void, then the result is void.
        if (mi.returnType().isVoid()) {
            return ts.Void();
        }

        // Otherwise, if unchecked conversion was necessary for the method to be applicable then the result type is the erasure (�4.6) of the method�s declared return type.
        // XXX how to check this? We need to implement it properly.

        // Otherwise, if the method being invoked is generic, then for 1 � i � n , 
        // let Fi be the formal type parameters of the method, let Ai be the actual type arguments inferred for the method invocation, and 
        // let R be the declared return type of the method being invoked. The result type is obtained by applying capture conversion (�5.1.10) to R[F1 := A1, ..., Fn := An].
        // --- mi has already had substitution applied, so it is covered by the following case.

        // Otherwise, the result type is obtained by applying capture conversion (�5.1.10) to the type given in the method declaration.
        return ts.applyCaptureConversion(mi.returnType(), this.node()
                                                              .position());
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        Call n = (Call) this.node();
        JL5CallExt ext = (JL5CallExt) JL5Ext.ext(n);

        if (!n.isTargetImplicit()) {
            if (n.target() instanceof Expr) {
                n.printSubExpr((Expr) n.target(), w, tr);
            }
            else if (n.target() != null) {
                if (tr instanceof JL5Translator) {
                    JL5Translator jltr = (JL5Translator) tr;
                    jltr.printReceiver(n.target(), w);
                }
                else {
                    ((Node_c) n).print(n.target(), w, tr);
                }
            }
            w.write(".");
            w.allowBreak(2, 3, "", 0);

            if (ext.typeArgs() != null && !ext.typeArgs().isEmpty()) {
                w.write("<");
                Iterator<TypeNode> it = ext.typeArgs().iterator();
                while (it.hasNext()) {
                    TypeNode tn = it.next();
                    ((Node_c) n).print(tn, w, tr);
                    if (it.hasNext()) {
                        w.write(",");
                        w.allowBreak(0, " ");
                    }
                }
                w.write(">");
                w.allowBreak(0, " ");
            }
        }

        w.begin(0);
        w.write(n.name() + "(");
        if (n.arguments().size() > 0) {
            w.allowBreak(2, 2, "", 0); // miser mode
            w.begin(0);

            for (Iterator<Expr> i = n.arguments().iterator(); i.hasNext();) {
                Expr e = i.next();
                ((Node_c) n).print(e, w, tr);

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

    public Type findContainer(TypeSystem ts, MethodInstance mi) {
        JL5TypeSystem jts = (JL5TypeSystem) ts;
        return jts.erasureType(mi.container());
    }

    public ReferenceType findTargetType() throws SemanticException {
        return ((CallOps) node()).findTargetType();
    }

    public Node typeCheckNullTarget(TypeChecker tc, List<Type> argTypes)
            throws SemanticException {
        return ((CallOps) node()).typeCheckNullTarget(tc, argTypes);
    }

}

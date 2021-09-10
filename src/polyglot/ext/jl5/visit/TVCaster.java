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
import java.util.List;

import polyglot.ast.ArrayInit;
import polyglot.ast.Assign;
import polyglot.ast.Binary;
import polyglot.ast.Call;
import polyglot.ast.Cast;
import polyglot.ast.Conditional;
import polyglot.ast.Eval;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.Lit;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Special;
import polyglot.ast.StringLit;
import polyglot.ast.Throw;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.JL5Options;
import polyglot.ext.jl5.ast.AnnotationElem;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.frontend.Job;
import polyglot.types.ArrayType;
import polyglot.types.FieldInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.NodeVisitor;

/**
 * Add casts so that we can erase generic classes and the code will still compile under Java 1.4.
 * We need to insert casts aggressively, although there is definitely opportunity to modify this
 * code to avoid redundant casts.
 *
 */
public class TVCaster extends AscriptionVisitor {
    public TVCaster(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public Expr ascribe(Expr e, Type toType) throws SemanticException {
        if (e.type() == null || toType == null || !toType.isCanonical()) {
            return e;
        }
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        Type fromType = ts.erasureType(e.type());
        toType = ts.erasureType(toType);
        if (!fromType.isReference() || !toType.isReference() || ts.Object().equals(toType)) {
            return e;
        }
        if (e instanceof Special || e instanceof ArrayInit || e instanceof Lit) {
            return e;
        }
        if (e instanceof New && ts.isImplicitCastValid(((New) e).objectType().type(), toType)) {
            return e;
        }
        if (isStringLiterals(e)) {
            return e;
        }
        if (e instanceof Field) {
            Field f = (Field) e;
            if (!mayBeParameterizedField(f.fieldInstance())) {
                return e;
            }
        }
        if (e instanceof Call) {
            Call c = (Call) e;
            if (!mayHaveParameterizedReturn(c.methodInstance())
                    && !mayHaveCovariantReturn(c.methodInstance())) {
                return e;
            }
        }

        if (ts.isCastValid(fromType, toType)) {
            Type castType = toType;
            if (toType.isClass() && !ts.classAccessible(toType.toClass(), this.context())) {
                // the toType is not accessible.
                // return e;
                // Let's try using the fromType instead.
                castType = fromType;
            }
            return insertCast(e, castType);
        }
        return e;
    }

    @Override
    public NodeVisitor enterCall(Node parent, Node n) throws SemanticException {
        if (n instanceof AnnotationElem) {
            return bypassChildren(n);
        }
        return super.enterCall(parent, n);
    }

    private boolean mayBeParameterizedField(FieldInstance fi) {
        ReferenceType container = fi.container();
        JL5ParsedClassType pct;
        if (container.isArray()) {
            Type base = container.toArray().base();
            while (base.isArray()) base = base.toArray().base();
            if (base instanceof TypeVariable) return true;
            if (base.isReference()) {
                pct = getBase(base.toReference());
            } else return false;
        } else pct = getBase(container);

        FieldInstance bfi = pct.fieldNamed(fi.name());
        if (bfi == null) {
            throw new InternalCompilerError(
                    "Couldn't find field named " + fi.name() + " in " + pct);
        }

        return hasTypeVariable(bfi.type());
    }

    private boolean mayHaveParameterizedReturn(MethodInstance mi) {
        // TODO fix this and document

        List<MethodInstance> overrides = new ArrayList<>();
        overrides.add(mi);
        overrides.addAll(ts.overrides(mi));
        overrides.addAll(ts.implemented(mi));
        for (MethodInstance mj : overrides) {
            if (mayHaveParameterizedReturnImpl(mj)) {
                return true;
            }
        }
        return false;
    }

    private boolean mayHaveParameterizedReturnImpl(MethodInstance mi) {
        ReferenceType container = mi.container();
        JL5ParsedClassType pct;
        if (container.isArray()) {
            Type base = container.toArray().base();
            while (base.isArray()) base = base.toArray().base();
            if (base instanceof TypeVariable) return true;
            if (base.isReference()) {
                pct = getBase(base.toReference());
            } else return false;
        } else pct = getBase(container);

        List<? extends MethodInstance> meths = pct.methodsNamed(mi.name());

        for (MethodInstance bmi : meths) {
            if (bmi.formalTypes().size() == mi.formalTypes().size()) {
                // might be the same method...
                if (hasTypeVariable(bmi.returnType())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean mayHaveCovariantReturn(MethodInstance mi) {
        if (mi.returnType().isReference()) {
            List<MethodInstance> overrides = ts.overrides(mi);
            overrides.addAll(ts.implemented(mi));
            ReferenceType ret = mi.returnType().toReference();
            for (MethodInstance ovr : overrides) {
                ReferenceType supRet = ovr.returnType().toReference();
                if (!ts.equals(ret, supRet)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasTypeVariable(Type t) {
        // really want a type visitor...
        if (t instanceof TypeVariable) {
            return true;
        }
        if (t instanceof ArrayType) {
            return hasTypeVariable(((ArrayType) t).base());
        }
        //        if (t instanceof JL5SubstClassType) {
        //            JL5SubstClassType sct = (JL5SubstClassType)t;
        //            if (hasTypeVariable(sct.base())) {
        //                return true;
        //            }
        //            for (Type s : (List<Type>)sct.actuals()) {
        //                if (hasTypeVariable(s)) {
        //                    return true;
        //                }
        //            }
        //        }
        return false;
    }

    private static JL5ParsedClassType getBase(ReferenceType container) {
        if (container instanceof JL5SubstClassType) {
            return ((JL5SubstClassType) container).base();
        } else if (container instanceof RawClass) {
            return ((RawClass) container).base();
        } else if (container instanceof JL5ParsedClassType) {
            return (JL5ParsedClassType) container;
        }
        throw new InternalCompilerError(
                "Don't know how to deal with container of type " + container.getClass());
    }

    /**
     * Does expression e consist only of string literals and concatenations of string literals?
     */
    private boolean isStringLiterals(Expr e) {
        if (e instanceof StringLit) {
            return true;
        }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            return b.operator() == Binary.ADD
                    && isStringLiterals(b.left())
                    && isStringLiterals(b.right());
        }
        return false;
    }

    private Expr insertCast(Expr e, Type toType) {
        if (toType.isClass() && toType.toClass().fullName().equals("java.lang.Enum")) {
            // it's the enum type.
            // see if we want to replace it
            JL5Options opts = (JL5Options) job.extensionInfo().getOptions();
            if (opts.removeJava5isms) {
                String enumImpl = opts.enumImplClass;
                if (!"java.lang.Enum".equals(enumImpl)) {
                    // we now have a difficult choice.
                    // Do we leave it as java.lang.Enum, or cast it to enumImpl?
                    // Let's cast it to the type of the expression instead...
                    Type eType = e.type();
                    JL5TypeSystem ts = (JL5TypeSystem) this.ts;
                    if (eType.isNull()) {
                        // the expression is 'null'. We shouldn't cast it because
                        // casting to null is illegal...
                        return e;
                    }
                    if (ts.erasureType(eType).equals(ts.erasureType(ts.Enum()))) {
                        // the type of eType is already Enum. So let's just leave it and hope.
                        return e;
                    }
                    toType = ts.erasureType(eType);
                }
            }
        }
        TypeNode tn = nf.CanonicalTypeNode(Position.compilerGenerated(), toType);
        Expr newE = nf.Cast(Position.compilerGenerated(), tn, e);
        return newE.type(toType);
    }

    @Override
    protected Node leaveCall(Node parent, Node old, Node n, NodeVisitor v)
            throws SemanticException {
        Node ret = super.leaveCall(parent, old, n, v);
        if (parent instanceof Eval && ret instanceof Cast) {
            // inserted one cast too many...
            Cast c = (Cast) ret;
            return c.expr();
        }
        if (parent instanceof Assign && ret instanceof Cast && ((Assign) parent).left() == old) {
            // inserted one cast too many...
            Cast c = (Cast) ret;
            return c.expr();
        }
        if (parent instanceof Throw && ret instanceof Cast) {
            // let's make sure we don't change an unchecked exception to a checked exception
            Cast c = (Cast) ret;
            c = c.castType(c.castType().type(c.expr().type()));
            c = (Cast) c.type(c.expr().type());
            ret = c;
        }
        if (parent instanceof Conditional) {
            Conditional c = (Conditional) parent;
            if (c.consequent() == old || c.alternative() == old) {
                // n is the consequent or alternative
                if (c.type().isReference() && !((Expr) n).type().equals(c.type())) {
                    // c is a reference type that's different from the type of the conditional.
                    // add a cast, since the Java 1.5 typing rules for conditionals are more
                    // permissive.
                    return insertCast((Expr) n, c.type());
                }
            }
        }
        if (parent instanceof Binary) {
            Binary b = (Binary) parent;
            if (b.type().equals(ts.String()) && ret instanceof Cast) {
                Cast c = (Cast) ret;
                if (c.castType().type().equals(ts.String())) {
                    // we have a cast to a String for a binary +
                    // e.g., "" + (String)o
                    // Get rid of the cast.
                    return c.expr();
                }
            }
        }

        // SNC: we were always casting the receiver of a call to the appropriate type
        // This does not seem to be required.
        //        if (parent instanceof Call && old == ((Call) parent).target()) {
        //            Call c = (Call) parent;
        //            if (c.target() instanceof Expr
        //                    && !(c.target() instanceof Special || c.target() instanceof Lit)) {
        //                Expr e = (Expr) n;
        //                if (e instanceof Cast) {
        //                    e = ((Cast) e).expr();
        //                }
        //                // cast e to the type of the container
        //                JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        //                Type t = c.methodInstance().container();
        //                Type et = ts.erasureType(t);
        //                return insertCast(e, et);
        //            }
        //        }
        return ret;
    }
}

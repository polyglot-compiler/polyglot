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
package polyglot.ext.jl5.types.inference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import polyglot.ext.jl5.types.AnnotationTypeElemInstance;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.JL5TypeSystem_c;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.WildCardType;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.ClassType_c;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.Package;
import polyglot.types.ReferenceType;
import polyglot.types.Resolver;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class LubType_c extends ClassType_c implements LubType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<ReferenceType> lubElems;

    public LubType_c(TypeSystem ts, Position pos, List<ReferenceType> lubElems) {
        super(ts, pos);
        this.lubElems = lubElems;
    }

    @Override
    public List<ReferenceType> lubElements() {
        return lubElems;
    }

    protected ReferenceType lubCalculated = null;

    @Override
    public ReferenceType calculateLub() {
        if (lubCalculated == null) {
            lubCalculated = lub_force();
        }
        return lubCalculated;
    }

    @Override
    public Kind kind() {
        return LUB;
    }

    private ReferenceType lub(ReferenceType... a) {
        List<ReferenceType> l = new ArrayList<>();
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        for (ReferenceType t : a) {
            l.add(t);
        }
        return ts.lub(this.position, l);
    }

    private ReferenceType lub_force() {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;

        // st is the set of all supertypes of the lubElems.        
        Set<ReferenceType> st = new LinkedHashSet<>();

        // est is the intersection of all erased supertypes of lubElems.
        // That is, during the loop below, it is the intersection of
        // the sets of erased supertypes of elements considered so far.
        // By the end of the loop it will be mec, the minimal erased 
        // candidate set (JLS 3rd ed, 15.12.2, p 464.)
        Set<ReferenceType> est = null;

        for (ReferenceType u : lubElems) {
            st.addAll(ts.allAncestorsOf(u));

            List<ReferenceType> u_supers = new ArrayList<>();
            for (ReferenceType u_super : ts.allAncestorsOf(u)) {
                if (u_super instanceof RawClass) {
                    u_supers.add(((RawClass) u_super).erased());
                }
                else u_supers.add(u_super);
            }

            Set<ReferenceType> est_of_u = new LinkedHashSet<>();
            for (ReferenceType super_of_u : u_supers) {
                if (super_of_u instanceof JL5SubstClassType) {
                    JL5SubstClassType g = (JL5SubstClassType) super_of_u;
                    est_of_u.add(g.base());
                }
                else est_of_u.add(super_of_u);
            }
            if (est == null) {
                est = new LinkedHashSet<>();
                est.addAll(est_of_u);
            }
            else {
                est.retainAll(est_of_u);
            }
        }
        Set<ReferenceType> mec = new LinkedHashSet<>(est);
        for (ReferenceType e1 : est) {
            for (ReferenceType e2 : est) {
                if (!ts.equals(e1, e2) && ts.isSubtype(e2, e1)) {
                    mec.remove(e1);
                    break;
                }
            }
        }
        List<ReferenceType> cand = new ArrayList<>();
        for (ReferenceType m : mec) {
            List<ReferenceType> inv = new ArrayList<>();
            for (ReferenceType t : st) {
                if (ts.equals(m, t) || t instanceof JL5SubstClassType
                        && ((JL5SubstClassType) t).base().equals(m)
                        || t instanceof RawClass
                        && ((RawClass) t).erased().base().equals(m)) {
                    inv.add(t);
                }
            }
            cand.add(lci(inv));
        }
        try {
            if (ts.checkIntersectionBounds(cand, true)) {
                return ts.intersectionType(this.position, cand);
            }
        }
        catch (SemanticException e) {
        }
        return ts.Object();
    }

    private ReferenceType lci(List<ReferenceType> inv) {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        ReferenceType first = inv.get(0);
        if (inv.size() == 1 || first instanceof RawClass
                || first instanceof JL5ParsedClassType) {
            return first;
        }
        JL5SubstClassType res = (JL5SubstClassType) first;
        for (int i = 1; i < inv.size(); i++) {
            ReferenceType next = inv.get(i);
            if (next instanceof RawClass || next instanceof JL5ParsedClassType) {
                return next;
            }
            List<ReferenceType> lcta_args = new ArrayList<>();
            JL5SubstClassType nextp = (JL5SubstClassType) next;
            for (int argi = 0; argi < res.actuals().size(); argi++) {
                ReferenceType a1 = res.actuals().get(argi);
                ReferenceType a2 = nextp.actuals().get(argi);
                lcta_args.add(lcta(a1, a2));
            }
            try {
                res =
                        (JL5SubstClassType) ts.instantiate(position,
                                                           res.base(),
                                                           lcta_args);
            }
            catch (SemanticException e) {
                throw new InternalCompilerError("Unexpected SemanticException",
                                                e);
            }
        }
        return res;
    }

    private ReferenceType lcta(ReferenceType a1, ReferenceType a2) {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        if (a1 instanceof WildCardType) {
            WildCardType a1wc = (WildCardType) a1;
            if (a2 instanceof WildCardType) {
                WildCardType a2wc = (WildCardType) a2;
                // a1 and a2 are wild cards
                if (a1wc.hasLowerBound() && a2wc.hasLowerBound()) {
                    return ts.wildCardType(position,
                                           null,
                                           glb(a1wc.lowerBound(),
                                               a2wc.lowerBound()));
                }
                if (a1wc.hasLowerBound()) {
                    // a1 has lower bound, a2 does not
                    if (a1wc.lowerBound().equals(a2wc.upperBound())) {
                        return a1wc.lowerBound();
                    }
                    else {
                        return ts.wildCardType(position);
                    }
                }
                if (a2wc.hasLowerBound()) {
                    // a2 has lower bound, a1 does not
                    if (a2wc.lowerBound().equals(a1wc.upperBound())) {
                        return a2wc.lowerBound();
                    }
                    else {
                        return ts.wildCardType(position);
                    }
                }
                // neither a1 nor a2 has a lower bound
                return ts.wildCardType(position,
                                       ts.lub(position,
                                              CollectionUtil.list(a1wc.upperBound(),
                                                                  a2wc.upperBound())),
                                       null);
            }
            // a1 is a wildcard, a2 is not
            if (a1wc.hasLowerBound()) {
                return ts.wildCardType(position,
                                       null,
                                       glb(a1wc.lowerBound(), a2));
            }
            else {
                return ts.wildCardType(position,
                                       lub(a1wc.upperBound(), a2),
                                       null);
            }
        }
        // a1 is not a wildcard
        if (a2 instanceof WildCardType) {
            WildCardType a2wc = (WildCardType) a2;
            // a1 is not a wildcard, a2 is a wildcard
            if (a2wc.hasLowerBound()) {
                return ts.wildCardType(position,
                                       null,
                                       glb(a1, a2wc.lowerBound()));
            }
            else {
                return ts.wildCardType(position,
                                       lub(a1, a2wc.upperBound()),
                                       null);
            }
        }

        // neither a1 nor a2 is a wildcard.
        if (ts.equals(a1, a2)) {
            return a1;
        }
        else {
            return ts.wildCardType(position,
                                   ts.lub(position, CollectionUtil.list(a1, a2)),
                                   null);
        }
    }

    private ReferenceType glb(ReferenceType t1, ReferenceType t2) {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        List<ReferenceType> l = CollectionUtil.list(t1, t2);
        try {
            if (!ts.checkIntersectionBounds(l, true)) {
                return ts.Object();
            }
            else {
                return ts.intersectionType(position, l);
            }
        }
        catch (SemanticException e) {
            return ts.Object();
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("lub(");
        sb.append(JL5TypeSystem_c.listToString(lubElems));
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean isCastValidImpl(Type toType) {
        for (Type elem : lubElements()) {
            if (!ts.isCastValid(elem, toType)) return false;
        }
        return true;
    }

    @Override
    public LinkedList<Type> isImplicitCastValidChainImpl(Type toType) {
        for (Type elem : lubElements()) {
            if (!ts.isImplicitCastValid(elem, toType)) return null;
        }

        LinkedList<Type> chain = new LinkedList<>();
        chain.add(this);
        chain.add(toType);
        return chain;
    }

    @Override
    public boolean isSubtypeImpl(Type ancestor) {
        for (Type elem : lubElements()) {
            if (!ts.isSubtype(elem, ancestor)) return false;
        }
        return true;
    }

    @Override
    public boolean isRawClass() {
        return false;
    }

    @Override
    public EnumInstance enumConstantNamed(String name) {
        return null;
    }

    @Override
    public List<EnumInstance> enumConstants() {
        return Collections.<EnumInstance> emptyList();
    }

    @Override
    public String translateAsReceiver(Resolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean inStaticContext() {
        return false;
    }

    @Override
    public void setFlags(Flags flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContainer(ReferenceType container) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Job job() {
        throw new UnsupportedOperationException();
    }

    @Override
    public polyglot.types.ClassType outer() {
        return null;
    }

    @Override
    public String name() {
        return this.toString();
    }

    @Override
    public Package package_() {
        return null;
    }

    @Override
    public Flags flags() {
        return Flags.PUBLIC.set(Flags.FINAL);
    }

    @Override
    public List<ConstructorInstance> constructors() {
        return Collections.<ConstructorInstance> emptyList();
    }

    @Override
    public List<ClassType> memberClasses() {
        return Collections.<ClassType> emptyList();
    }

    @Override
    public List<? extends MethodInstance> methods() {
        return calculateLub().methods();
    }

    @Override
    public List<? extends FieldInstance> fields() {
        return calculateLub().fields();
    }

    @Override
    public List<? extends ReferenceType> interfaces() {
        return calculateLub().interfaces();
    }

    @Override
    public Set<? extends Type> superclasses() {
        Type lub = calculateLub();
        if (lub instanceof JL5ClassType) {
            JL5ClassType ct = (JL5ClassType) lub;
            return ct.superclasses();
        }
        return Collections.<Type> singleton(superType());
    }

    @Override
    public Type superType() {
        return calculateLub().superType();
    }

    @Override
    public AnnotationTypeElemInstance annotationElemNamed(String name) {
        return null;
    }

    @Override
    public List<AnnotationTypeElemInstance> annotationElems() {
        return Collections.<AnnotationTypeElemInstance> emptyList();
    }

    @Override
    public Annotations annotations() {
        return ((JL5TypeSystem) this.typeSystem()).NoAnnotations();
    }

}

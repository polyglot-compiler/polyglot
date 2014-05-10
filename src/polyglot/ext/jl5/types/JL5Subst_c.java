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
package polyglot.ext.jl5.types;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polyglot.ext.param.types.ParamTypeSystem;
import polyglot.ext.param.types.Subst_c;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

public class JL5Subst_c extends Subst_c<TypeVariable, ReferenceType> implements
        JL5Subst {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public JL5Subst_c(ParamTypeSystem<TypeVariable, ReferenceType> ts,
            Map<TypeVariable, ? extends ReferenceType> subst) {
        super(ts, subst);
//        if (subst.isEmpty()) {
//            Thread.dumpStack();
//        }
    }

    @Override
    public Type substType(Type t) {
        if (t instanceof TypeVariable) {
            return substTypeVariable((TypeVariable) t);
        }
        if (t instanceof WildCardType) {
            return substWildCardTypeVariable((WildCardType) t);
        }
        if (t instanceof IntersectionType) {
            return substIntersectionType((IntersectionType) t);
        }

        return super.substType(t);
    }

    // when substituting type variables that aren't in the subst map, 
    // keep track of which ones we have seen already, so that we don't go into 
    // an infinite loop as we subst on their upper bounds.
    private LinkedList<TypeVariable> visitedTypeVars = new LinkedList<>();

    public ReferenceType substTypeVariable(TypeVariable t) {
        if (subst.containsKey(t)) {
            return subst.get(t);
        }
        if (visitedTypeVars.contains(t)) {
            return t;
        }
        for (TypeVariable k : subst.keySet()) {
            if (k.equals(t)) {
                return subst.get(k);
            }
        }
        visitedTypeVars.addLast(t);
        TypeVariable origT = t;
        t = t.upperBound((ReferenceType) substType(t.upperBound()));

        if (visitedTypeVars.removeLast() != origT) {
            throw new InternalCompilerError("Unexpected type variable was last on the list");
        }
        return t;
    }

    public Type substWildCardTypeVariable(WildCardType t) {
        WildCardType n = t;
        n = n.upperBound((ReferenceType) substType(t.upperBound()));
        n = n.lowerBound((ReferenceType) substType(t.lowerBound()));
        return n;
    }

    public Type substIntersectionType(IntersectionType t) {
        List<ReferenceType> s = this.substTypeList(t.bounds());
        t = (IntersectionType) t.copy();
        t.setBounds(s);
        return t;
    }

    @Override
    protected ReferenceType substSubstValue(ReferenceType value) {
        return (ReferenceType) substType(value);
    }

    @Override
    protected ClassType substClassTypeImpl(ClassType t) {
        // Don't bother trying to substitute into a non-JL5 class.
        if (!(t instanceof JL5ClassType)) {
            return t;
        }

        if (t instanceof RawClass) {
            // don't substitute raw classes
            return t;
        }
        if (t instanceof JL5SubstClassType) {
            // this case should be impossible
            throw new InternalCompilerError("Should have no JL5SubstClassTypes");
        }

        if (t instanceof JL5ParsedClassType) {
            JL5ParsedClassType pct = (JL5ParsedClassType) t;
            JL5TypeSystem ts = (JL5TypeSystem) this.ts;
            List<TypeVariable> typeVars =
                    ts.classAndEnclosingTypeVariables(pct);
            // are the type variables of pct actually relevant to this subst? If not, then return the pct.
            boolean typeVarsRelevant = false;
            for (TypeVariable tv : typeVars) {
                if (this.substitutions().containsKey(tv)) {
                    typeVarsRelevant = true;
                    break;
                }
            }
            if (!typeVarsRelevant) {
                // no parameters to be instantiated!
                return pct;
            }

            return new JL5SubstClassType_c(ts, t.position(), pct, this);
        }

        throw new InternalCompilerError("Don't know how to handle class type "
                + t.getClass());
    }

    @Override
    public <T extends MethodInstance> T substMethod(T mi) {
        JL5MethodInstance mj = (JL5MethodInstance) super.substMethod(mi);
        if (mj.typeParams() != null && !mj.typeParams().isEmpty()) {
            // remove any type params we have replaced.
            List<TypeVariable> l = new ArrayList<>(mj.typeParams());
            l.removeAll(this.subst.keySet());
            mj.setTypeParams(l);
        }
        // subst the type params
        mj.setTypeParams(this.<TypeVariable> substTypeList(mj.typeParams()));

        @SuppressWarnings("unchecked")
        T result = (T) mj;
        return result;
    }

    @Override
    public <T extends ConstructorInstance> T substConstructor(T ci) {
        JL5ConstructorInstance cj =
                (JL5ConstructorInstance) super.substConstructor(ci);
        if (cj.typeParams() != null && !cj.typeParams().isEmpty()) {
            // remove any type params we have replaced.
            List<TypeVariable> l = new ArrayList<>(cj.typeParams());
            l.removeAll(this.subst.keySet());
            cj.setTypeParams(l);
        }

        // subst the type params
        cj.setTypeParams(this.<TypeVariable> substTypeList(cj.typeParams()));

        @SuppressWarnings("unchecked")
        T result = (T) cj;
        return result;
    }

    @Override
    protected ReferenceType substContainer(MemberInstance mi) {
        if (mi.flags().isStatic()) return mi.container();
        return super.substContainer(mi);
    }

    @Override
    public JL5ProcedureInstance substProcedure(JL5ProcedureInstance mi) {
        if (mi instanceof MethodInstance) {
            return (JL5ProcedureInstance) substMethod((MethodInstance) mi);
        }
        else {
            return (JL5ProcedureInstance) substConstructor((ConstructorInstance) mi);
        }
    }
}

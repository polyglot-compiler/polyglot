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

import java.util.List;
import java.util.Map;

import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.CachingTransformingList;
import polyglot.util.SerialVersionUID;
import polyglot.util.Transformation;

/**
 * Subst for a raw type (See JLS 3rd ed Sec 4.8.)
 * Some substitution behavior differs for raw types.
 *
 */
public class JL5RawSubst_c extends JL5Subst_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    private final JL5ParsedClassType base;

    public JL5RawSubst_c(JL5TypeSystem ts,
            Map<TypeVariable, ReferenceType> subst, JL5ParsedClassType base) {
        super(ts, subst);
        this.base = base;
    }

    @Override
    protected ClassType substClassTypeImpl(ClassType t) {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        if (base.equals(t)) {
            return ts.rawClass(base);
        }
        return super.substClassTypeImpl(t);
    }

    @Override
    public <T extends MethodInstance> T substMethod(T mi) {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        if (!base.equals(mi.container())) {
            return super.substMethod(mi);
        }

        // mi is a member of the raw class we are substituting.
        if (mi.flags().isStatic()) {
            // static method!
            // JLS 3rd ed 4.8: "The type of a static member of a raw type C is the same as its type in the generic declaration corresponding to C."
            @SuppressWarnings("unchecked")
            T result = (T) mi.declaration();
            return result;
        }
        // The type of a constructor (�8.8), instance method (�8.8, �9.4), or non-static field (�8.3) M 
        // of a raw type C that is not inherited from its superclasses or super- interfaces is the erasure of 
        // its type in the generic declaration corresponding to C.

        JL5MethodInstance mj = (JL5MethodInstance) mi.declaration();

        Type rt = ts.erasureType(mj.returnType());

        List<? extends Type> formalTypes = mj.formalTypes();
        formalTypes = eraseTypeList(formalTypes);

        List<? extends Type> throwTypes = mj.throwTypes();
        throwTypes = eraseTypeList(throwTypes);

        JL5MethodInstance tmpMi = (JL5MethodInstance) mj.copy();
        tmpMi.setReturnType(rt);
        tmpMi.setFormalTypes(formalTypes);
        tmpMi.setThrowTypes(throwTypes);
        tmpMi.setContainer(ts.rawClass(base));

        // subst the type params (that is, the method may have some type params that mention in their bounds 
        // type params from a containing class, which we will 
        // substitute.)
        tmpMi.setTypeParams(this.<TypeVariable> substTypeList(tmpMi.typeParams()));

        // now erase the type params, if there are any
        JL5Subst eraseMI = ts.erasureSubst(tmpMi);
        if (eraseMI != null) {
            tmpMi = eraseMI.substMethod(tmpMi);
        }

        @SuppressWarnings("unchecked")
        T result = (T) tmpMi;
        return result;
    }

    @Override
    public <T extends ConstructorInstance> T substConstructor(T ci) {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        if (!base.equals(ci.container())) {
            return super.substConstructor(ci);
        }

        // ci is a member of the raw class we are substituting.

        // The type of a constructor (�8.8), instance method (�8.8, �9.4), or non-static field (�8.3) M 
        // of a raw type C that is not inherited from its superclasses or super- interfaces is the erasure of 
        // its type in the generic declaration corresponding to C.

        JL5ConstructorInstance cj = (JL5ConstructorInstance) ci.declaration();

        List<? extends Type> formalTypes = cj.formalTypes();
        formalTypes = eraseTypeList(formalTypes);

        List<? extends Type> throwTypes = cj.throwTypes();
        throwTypes = eraseTypeList(throwTypes);

        JL5ConstructorInstance tmpCi = (JL5ConstructorInstance) cj.copy();
        tmpCi.setFormalTypes(formalTypes);
        tmpCi.setThrowTypes(throwTypes);
        tmpCi.setContainer(ts.rawClass(base));

        for (Object o : tmpCi.typeParams()) {
            if (!(o instanceof TypeVariable)) {
                System.err.println("Pi is " + tmpCi + " and type params is "
                        + tmpCi.typeParams() + " " + o + " " + o.getClass());
                System.err.println("Subst is " + this);
                Thread.dumpStack();
            }
        }

        // subst the type params
        tmpCi.setTypeParams(this.<TypeVariable> substTypeList(tmpCi.typeParams()));

        // now erase the type params, if there are any
        JL5Subst eraseCI = ts.erasureSubst(tmpCi);
        if (eraseCI != null) {
            tmpCi = eraseCI.substConstructor(tmpCi);
        }

        @SuppressWarnings("unchecked")
        T result = (T) tmpCi;
        return result;
    }

    public List<Type> eraseTypeList(List<? extends Type> list) {
        return new CachingTransformingList<>(list, TypeErase);
    }

    /** Function object for transforming types. */
    private final Transformation<Type, Type> TypeErase =
            new Transformation<Type, Type>() {
                @Override
                public Type transform(Type o) {
                    return ((JL5TypeSystem) typeSystem()).erasureType(o);
                }
            };

}

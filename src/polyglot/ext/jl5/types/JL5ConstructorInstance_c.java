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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.ConstructorInstance_c;
import polyglot.types.Flags;
import polyglot.types.Type;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class JL5ConstructorInstance_c extends ConstructorInstance_c implements
JL5ConstructorInstance {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<TypeVariable> typeParams;
    protected Annotations annotations;

    public JL5ConstructorInstance_c(JL5TypeSystem_c ts, Position pos,
            ClassType container, Flags flags, List<? extends Type> argTypes,
            List<? extends Type> excTypes,
            List<? extends TypeVariable> typeParams) {
        super(ts, pos, container, flags, argTypes, excTypes);
        this.typeParams = ListUtil.copy(typeParams, true);
        // Set the declaring procedure of the type vars
        for (TypeVariable tv : typeParams) {
            tv.setDeclaringProcedure((JL5ProcedureInstance) declaration());
        }
    }

    @Override
    public boolean isVariableArity() {
        return JL5Flags.isVarArgs(this.flags());
    }

    @Override
    public boolean callValidImpl(List<? extends Type> argTypes) {
        List<Type> myFormalTypes = formalTypes;

        // System.err.println("JL5MethodInstance_c callValid Impl " + this +
        // " called with " +argTypes);
        // now compare myFormalTypes to argTypes
        if (!isVariableArity() && argTypes.size() != myFormalTypes.size()) {
            return false;
        }
        if (isVariableArity() && argTypes.size() < myFormalTypes.size() - 1) {
            // the last (variable) argument can consume 0 or more of the actual
            // arguments.
            return false;
        }

        // Here, argTypes has at least myFormalTypes.size()-1 elements.
        Iterator<Type> formalTypes = myFormalTypes.iterator();
        Iterator<? extends Type> actualTypes = argTypes.iterator();
        Type formal = null;
        while (actualTypes.hasNext()) {
            Type actual = actualTypes.next();
            if (formalTypes.hasNext()) {
                formal = formalTypes.next();
            }
            if (!formalTypes.hasNext() && isVariableArity()) {
                // variable arity method, and this is the last arg.
                ArrayType arr =
                        (ArrayType) myFormalTypes.get(myFormalTypes.size() - 1);
                formal = arr.base();
            }
            if (ts.isImplicitCastValid(actual, formal)) {
                // Yep, this type is OK. Try the next one.
                continue;
            }
            // the actual can't be cast to the formal.
            // HOWEVER: there is still hope.
            if (isVariableArity() && myFormalTypes.size() == argTypes.size()
                    && !formalTypes.hasNext()) {
                // This is a variable arity method (e.g., m(int x,
                // String[])) and there
                // are the same number of actual arguments as formal
                // arguments.
                // The last actual can be either the base type of the array,
                // or the array type.
                ArrayType arr =
                        (ArrayType) myFormalTypes.get(myFormalTypes.size() - 1);
                if (!ts.isImplicitCastValid(actual, arr)) {
                    // System.err.println("     3: failed " + actual +
                    // " to " +formal + " and " + actual + " to " + arr);
                    return false;
                }
            }
            else {
                // System.err.println("     4: failed " + actual + " to "
                // +formal);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isSameConstructorImpl(ConstructorInstance ci) {
        if (!(ci instanceof JL5ConstructorInstance)) return false;
        JL5TypeSystem ts = (JL5TypeSystem) typeSystem();
        return ts.areOverrideEquivalent(this, (JL5ConstructorInstance) ci);
    }

    @Override
    public boolean isCanonical() {
        return super.isCanonical() && listIsCanonical(typeParams);
    }

    @Override
    public void setTypeParams(List<TypeVariable> typeParams) {
        this.typeParams = typeParams;
        // Set the declaring procedure of the type vars
        for (TypeVariable tv : typeParams) {
            tv.setDeclaringProcedure((JL5ProcedureInstance) declaration());
        }

    }

    @Override
    public List<TypeVariable> typeParams() {
        return Collections.unmodifiableList(typeParams);
    }

    @Override
    public JL5Subst erasureSubst() {
        JL5TypeSystem ts = (JL5TypeSystem) typeSystem();
        return ts.erasureSubst(this);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(designator());
        sb.append(" ");
        sb.append(flags.translate());
        if (!typeParams.isEmpty()) {
            sb.append("<");
            Iterator<TypeVariable> iter = typeParams().iterator();
            while (iter.hasNext()) {
                sb.append(iter.next());
                if (iter.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append("> ");
        }
        sb.append(signature());

        if (!throwTypes.isEmpty()) {
            sb.append(" throws ");
            for (Iterator<Type> i = throwTypes.iterator(); i.hasNext();) {
                Object o = i.next();
                sb.append(o.toString());

                if (i.hasNext()) {
                    sb.append(", ");
                }
            }
        }

        return sb.toString();
    }

    @Override
    public Annotations annotations() {
        return annotations;
    }

    @Override
    public void setAnnotations(Annotations annotations) {
        this.annotations = annotations;
    }

}

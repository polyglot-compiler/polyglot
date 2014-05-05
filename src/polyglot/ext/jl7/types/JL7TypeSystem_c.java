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
package polyglot.ext.jl7.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import polyglot.ext.jl5.types.JL5ConstructorInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5ProcedureInstance;
import polyglot.ext.jl5.types.JL5Subst;
import polyglot.ext.jl5.types.JL5TypeSystem_c;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.types.inference.InferenceSolver;
import polyglot.ext.jl7.types.inference.JL7InferenceSolver_c;
import polyglot.main.Report;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.NoMemberException;
import polyglot.types.ProcedureInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;

public class JL7TypeSystem_c extends JL5TypeSystem_c implements JL7TypeSystem {

    protected ClassType AUTOCLOSEABLE_;

    @Override
    public ClassType AutoCloseable() {
        if (AUTOCLOSEABLE_ != null) return AUTOCLOSEABLE_;
        return AUTOCLOSEABLE_ = load("java.lang.AutoCloseable");
    }

    @Override
    public DiamondType diamondType(Position pos, JL5ParsedClassType base) {
        return new DiamondType_c(pos, base);
    }

    @Override
    public ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> typeArgs, ClassType currClass,
            boolean fromClient) throws SemanticException {
        return findConstructor(container,
                               argTypes,
                               typeArgs,
                               currClass,
                               null,
                               fromClient);
    }

    @Override
    public ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> typeArgs, ClassType currClass,
            Type expectedObjectType, boolean fromClient)
            throws SemanticException {
        assert_(container);
        assert_(argTypes);

        List<ConstructorInstance> acceptable =
                findAcceptableConstructors(container,
                                           argTypes,
                                           typeArgs,
                                           currClass,
                                           expectedObjectType,
                                           fromClient);

        if (acceptable.size() == 0) {
            throw new NoMemberException(NoMemberException.CONSTRUCTOR,
                                        "No valid constructor found for "
                                                + container + "("
                                                + listToString(argTypes) + ").");
        }

        Collection<ConstructorInstance> maximal =
                findMostSpecificProcedures(acceptable);

        if (maximal.size() > 1) {
            StringBuffer sb = new StringBuffer();
            for (Iterator<ConstructorInstance> i = maximal.iterator(); i.hasNext();) {
                ConstructorInstance ci = i.next();
                sb.append(ci.container());
                sb.append(".");
                sb.append(ci.signature());
                if (i.hasNext()) {
                    if (maximal.size() == 2) {
                        sb.append(" and ");
                    }
                    else {
                        sb.append(", ");
                    }
                }
            }

            throw new SemanticException("Reference to " + container
                    + " is ambiguous, multiple constructors match: "
                    + sb.toString());
        }

        ConstructorInstance ci = maximal.iterator().next();
        return ci;
    }

    @Override
    protected List<ConstructorInstance> findAcceptableConstructors(
            ClassType container, List<? extends Type> argTypes,
            List<? extends ReferenceType> actualTypeArgs, ClassType currClass,
            boolean fromClient) throws SemanticException {
        return findAcceptableConstructors(container,
                                          argTypes,
                                          actualTypeArgs,
                                          currClass,
                                          null,
                                          fromClient);
    }

    /**
     * Populates the list acceptable with those ConstructorInstances which are
     * Applicable and Accessible as defined by JLS 15.12.2
     * @throws SemanticException 
     */
    protected List<ConstructorInstance> findAcceptableConstructors(
            ClassType container, List<? extends Type> argTypes,
            List<? extends ReferenceType> actualTypeArgs, ClassType currClass,
            Type expectedObjectType, boolean fromClient)
            throws SemanticException {
        assert_(container);
        assert_(argTypes);

        // apply capture conversion to container
        container =
                (ClassType) applyCaptureConversion(container,
                                                   container.position());

        SemanticException error = null;

        // List of constructors accessible from curClass that have valid new
        // call without boxing/unboxing conversion or variable arity and
        // are not overridden by an unaccessible constructor
        List<ConstructorInstance> phase1constructors = new ArrayList<>();
        // List of constructors accessible from curClass that have a valid new
        // call relying on boxing/unboxing conversion
        List<ConstructorInstance> phase2constructors = new ArrayList<>();
        // List of constructors accessible from curClass that have a valid new
        // call relying on boxing/unboxing conversion and variable arity
        List<ConstructorInstance> phase3constructors = new ArrayList<>();

        if (Report.should_report(Report.types, 2))
            Report.report(2, "Searching type " + container
                    + " for constructor " + container + "("
                    + listToString(argTypes) + ")");
        @SuppressWarnings("unchecked")
        List<JL5ConstructorInstance> constructors =
                (List<JL5ConstructorInstance>) container.constructors();
        for (JL5ConstructorInstance ci : constructors) {
            if (Report.should_report(Report.types, 3))
                Report.report(3, "Trying " + ci);

            JL5ConstructorInstance substCi =
                    (JL5ConstructorInstance) callValid(ci,
                                                       argTypes,
                                                       actualTypeArgs,
                                                       expectedObjectType);
            if (substCi != null) {
                ci = substCi;
                if (isAccessible(ci, currClass, fromClient)) {
                    if (Report.should_report(Report.types, 3))
                        Report.report(3, "->acceptable: " + ci);
                    if (varArgsRequired(ci))
                        phase3constructors.add(ci);
                    else if (boxingRequired(ci, argTypes))
                        phase2constructors.add(ci);
                    else phase1constructors.add(ci);
                }
                else {
                    if (error == null) {
                        error =
                                new NoMemberException(NoMemberException.CONSTRUCTOR,
                                                      "Constructor "
                                                              + ci.signature()
                                                              + " is inaccessible.");
                    }
                }
            }
            else {
                if (error == null) {
                    error =
                            new NoMemberException(NoMemberException.CONSTRUCTOR,
                                                  "Constructor "
                                                          + ci.signature()
                                                          + " cannot be invoked with arguments "
                                                          + "("
                                                          + listToString(argTypes)
                                                          + ").");
                }
            }
        }

        if (!phase1constructors.isEmpty()) return phase1constructors;
        if (!phase2constructors.isEmpty()) return phase2constructors;
        if (!phase3constructors.isEmpty()) return phase3constructors;

        if (error == null) {
            error =
                    new NoMemberException(NoMemberException.CONSTRUCTOR,
                                          "No valid constructor found for "
                                                  + container + "("
                                                  + listToString(argTypes)
                                                  + ").");
        }

        throw error;
    }

    @Override
    public boolean callValid(ProcedureInstance mi, List<? extends Type> argTypes) {
        return this.callValid((JL5ProcedureInstance) mi, argTypes, null, null) != null;
    }

    @Override
    public JL5ProcedureInstance callValid(JL5ProcedureInstance pi,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> actualTypeArgs,
            Type expectedReturnType) {
        if (actualTypeArgs == null) {
            actualTypeArgs = Collections.emptyList();
        }

        // First check that the number of arguments is reasonable
        if (argTypes.size() != pi.formalTypes().size()) {
            // the actual args don't match the number of the formal args.
            if (!(pi.isVariableArity() && argTypes.size() >= pi.formalTypes()
                                                               .size() - 1)) {
                // the last (variable) argument can consume 0 or more of the actual arguments.
                return null;
            }

        }

        JL5Subst subst = null;
        ReferenceType ct = pi.container();
        if (ct instanceof JL5ParsedClassType
                && !((JL5ParsedClassType) ct).typeVariables().isEmpty()
                || !pi.typeParams().isEmpty() && actualTypeArgs.isEmpty()) {
            // need to perform type inference
            subst = inferTypeArgs(pi, argTypes, expectedReturnType);
        }
        else if (!pi.typeParams().isEmpty() && !actualTypeArgs.isEmpty()) {
            Map<TypeVariable, ReferenceType> m = new HashMap<>();
            Iterator<? extends ReferenceType> iter = actualTypeArgs.iterator();
            for (TypeVariable tv : pi.typeParams()) {
                m.put(tv, iter.next());
            }
            subst = (JL5Subst) this.subst(m);
        }

        JL5ProcedureInstance mj = pi;
        if ((ct instanceof JL5ParsedClassType
                && !((JL5ParsedClassType) ct).typeVariables().isEmpty() || !pi.typeParams()
                                                                              .isEmpty())
                && subst != null) {
            // check that the substitution satisfies the bounds

            for (TypeVariable tv : subst.substitutions().keySet()) {
                Type a = subst.substitutions().get(tv);
                if (!isSubtype(a, tv.upperBound())) {
                    return null;
                }
            }

            mj = subst.substProcedure(pi);
        }

        if (super.callValid(mj, argTypes)) {
            return mj;
        }

        return null;
    }

    @Override
    protected InferenceSolver inferenceSolver(JL5ProcedureInstance pi,
            List<? extends Type> argTypes) {
        return new JL7InferenceSolver_c(pi, argTypes, this);
    }
}

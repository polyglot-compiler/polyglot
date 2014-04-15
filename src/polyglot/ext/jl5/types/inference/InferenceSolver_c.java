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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ext.jl5.JL5Options;
import polyglot.ext.jl5.types.JL5ArrayType;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5ProcedureInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.param.types.Subst;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.Position;

public class InferenceSolver_c implements InferenceSolver {

    private JL5TypeSystem ts;

    private JL5ProcedureInstance pi;

    private List<? extends Type> actualArgumentTypes;

    private List<? extends Type> formalTypes;

    private List<TypeVariable> typeVariablesToSolve;

    public InferenceSolver_c(JL5ProcedureInstance pi,
            List<? extends Type> actuals, JL5TypeSystem ts) {
        this.pi = pi;
        this.typeVariablesToSolve = typeVariablesToSolve(pi);
        this.actualArgumentTypes = actuals;
        this.formalTypes = pi.formalTypes();
        this.ts = ts;
    }

    protected List<TypeVariable> typeVariablesToSolve(JL5ProcedureInstance pi) {
        return pi.typeParams();
    }

    @Override
    public boolean isTargetTypeVariable(Type t) {
        if (t instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) t;
            return typeVariablesToSolve().contains(tv);
        }
        return false;
    }

    @Override
    public List<TypeVariable> typeVariablesToSolve() {
        return typeVariablesToSolve;
    }

    private Type[] solve(List<Constraint> constraints,
            boolean useSubtypeConstraints, boolean useSupertypeConstraints) {
        List<EqualConstraint> equals = new ArrayList<>();
        List<SubTypeConstraint> subs = new ArrayList<>();
        List<SuperTypeConstraint> supers = new ArrayList<>();
//        System.err.println("**** inference solver:");
//        System.err.println("      constraints : " + constraints);
//        System.err.println("      use subs? : " + useSubtypeConstraints
//                + "   use sups? : " + useSupertypeConstraints);
//        System.err.println("      variables : " + typeVariablesToSolve());

        while (!constraints.isEmpty()) {
            Constraint head = constraints.remove(0);
            if (head.canSimplify()) {
                List<Constraint> simps = head.simplify();
                constraints.addAll(0, simps);
            }
            else {
                if (head instanceof EqualConstraint) {
                    EqualConstraint eq = (EqualConstraint) head;
                    equals.add(eq);
                }
                else if (head instanceof SubTypeConstraint) {
                    SubTypeConstraint sub = (SubTypeConstraint) head;
                    subs.add(sub);
                }
                else if (head instanceof SuperTypeConstraint) {
                    SuperTypeConstraint sup = (SuperTypeConstraint) head;
                    supers.add(sup);
                }
            }
        }
//        System.err.println("      a equals : " + equals);
//        System.err.println("      a subs   : " + subs);
//        System.err.println("      a supers : " + supers);

        Comparator<Constraint> comp = new Comparator<Constraint>() {
            @Override
            public int compare(Constraint o1, Constraint o2) {
                return typeVariablesToSolve().indexOf(o1.formal)
                        - typeVariablesToSolve().indexOf(o2.formal);
            }
        };
        Collections.sort(equals, comp);
        Collections.sort(subs, comp);
        Collections.sort(supers, comp);

//        System.err.println("      equals : " + equals);
//        System.err.println("      subs   : " + subs);
//        System.err.println("      supers : " + supers);

        Type[] solution = new Type[typeVariablesToSolve().size()];
        for (EqualConstraint eq : equals) {
            int i = typeVariablesToSolve().indexOf(eq.formal);
            if ((solution[i] != null) && (!ts.equals(eq.actual, solution[i]))) {
                // incompatible equality constraints!
                // No solution.
                return null;
            }
            else {
                solution[i] = eq.actual;
            }
        }
        List<? extends Constraint> subSupConstraints =
                useSubtypeConstraints ? subs : supers;
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == null) {
                TypeVariable toSolve = typeVariablesToSolve().get(i);
                Set<ReferenceType> bounds = new LinkedHashSet<>();
                for (Constraint c : subSupConstraints) {
                    if (c.formal.equals(toSolve) && c.actual.isReference()) {
                        bounds.add((ReferenceType) c.actual);
                    }
                }
                List<ReferenceType> u = new ArrayList<>(bounds);
                if (u.size() == 1) {
                    solution[i] = u.get(0);
                }
                else if (u.size() > 1) {
                    if (useSubtypeConstraints) {
                        solution[i] = ts.lub(Position.compilerGenerated(), u);
                        // check that the bounds hold in the presence of lubs
                        if (!solution[i].isSubtype(toSolve.upperBound())) {
                            return null;
                        }
                    }
                    else {
                        // supertype Constraints
                        solution[i] = ts.glb(Position.compilerGenerated(), u);
                    }
                }
            }
        }

//        System.err.println("      Solution : " + Arrays.asList(solution));

        return solution;
    }

    private List<Constraint> getInitialConstraints() {
        List<Constraint> constraints = new ArrayList<>();
        int numFormals = formalTypes.size();
        for (int i = 0; i < numFormals - 1; i++) {
            constraints.add(new SubConversionConstraint(actualArgumentTypes.get(i),
                                                        formalTypes.get(i),
                                                        this));
        }
        if (numFormals > 0) {
            if (pi != null && JL5Flags.isVarArgs(pi.flags())) {
                JL5ArrayType lastFormal =
                        (JL5ArrayType) pi.formalTypes().get(numFormals - 1);
                if (actualArgumentTypes.size() == numFormals
                        && ((Type) actualArgumentTypes.get(numFormals - 1)).isArray()) {
                    // there are the same number of actuals as formals, and the last actual is an array type.
                    // So the last actual must be convertible to the array type.
                    constraints.add(new SubConversionConstraint(actualArgumentTypes.get(numFormals - 1),
                                                                formalTypes.get(numFormals - 1),
                                                                this));
                }
                else {
                    // more than one remaining actual, or the last remaining actual is not an array type.
                    // all remaining actuals must be convertible to the basetype of the last (i.e. varargs) formal.
                    for (int i = numFormals - 1; i < actualArgumentTypes.size(); i++) {
                        constraints.add(new SubConversionConstraint(actualArgumentTypes.get(i),
                                                                    lastFormal.base(),
                                                                    this));
                    }
                }
            }
            else if (numFormals == actualArgumentTypes.size()) {
                // not a varargs method
                constraints.add(new SubConversionConstraint(actualArgumentTypes.get(numFormals - 1),
                                                            formalTypes.get(numFormals - 1),
                                                            this));
            }
        }
        return constraints;
    }

    @Override
    public Map<TypeVariable, ReferenceType> solve(Type expectedReturnType) {
        // first, solve without considering the return type
        Type[] solution = this.solve(getInitialConstraints(), true, false);

        if (solution == null) {
            // no solution
            return null;
        }

        if (hasUnresolvedTypeArguments(solution)) {
            solution = handleUnresolvedTypeArgs(solution, expectedReturnType);
        }
        else {
            // resolved all type arguments. Do we want to try to be more permissive?
            JL5Options opts = (JL5Options) ts.extensionInfo().getOptions();
            Type returnType = returnType(pi);
            if (opts.morePermissiveInference && returnType != null
                    && returnType.isReference() && !returnType.isVoid()
                    && expectedReturnType != null) {
                // Try to perform a more permissive inference where we take the
                // expected return type into consideration, even if the the previous
                // step finds a solution for all type variables. This may find a better one.
                // We do this for compatibility with javac, which appears to deviate
                // from the Java Language Spec on type inference.

                List<Constraint> cons = new ArrayList<>();
                cons.addAll(getInitialConstraints());
                cons.add(new SuperConversionConstraint(expectedReturnType,
                                                       returnType,
                                                       this));
                Type[] betterSolution = this.solve(cons, true, false);
                if (betterSolution != null) {
//                    System.err.println("Found a better solution: " + Arrays.asList(betterSolution));
                    solution = betterSolution;
                }
            }
        }

        Map<TypeVariable, ReferenceType> m = new LinkedHashMap<>();
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == null) {
                // no solution for this variable.
                solution[i] = ts.Object();
            }
            m.put(typeVariablesToSolve().get(i), (ReferenceType) solution[i]);
        }
        return m;
    }

    private Type[] handleUnresolvedTypeArgs(Type[] solution,
            Type expectedReturnType) {
        List<Constraint> constraints = new ArrayList<>();
        constraints.addAll(this.getInitialConstraints());

        // get the substitution corresponding to the solution so far
        Map<TypeVariable, ReferenceType> m = new LinkedHashMap<>();
        for (int i = 0; i < solution.length; i++) {
            ReferenceType t = (ReferenceType) solution[i];
            if (t == null) {
                t = typeVariablesToSolve().get(i);
            }
            m.put(typeVariablesToSolve().get(i), t);
        }
        Subst<TypeVariable, ReferenceType> subst = ts.subst(m);

        // see if the return type is appropriate for inferring additional results
        Type returnType = returnType(pi);
        if (returnType != null && returnType.isReference()
                && !returnType.isVoid()) {
            // See JLS 3rd ed, 15.12.2.8
            if (expectedReturnType == null) {
                expectedReturnType = ts.Object();
            }
            Type rt = subst.substType(returnType);
            constraints.add(new SuperConversionConstraint(expectedReturnType,
                                                          rt,
                                                          this));
        }

        for (int i = 0; i < solution.length; i++) {
            TypeVariable ti = typeVariablesToSolve().get(i);
            Type bi = subst.substType(ti.upperBound());
            constraints.add(new SuperConversionConstraint(bi, ti, this));
        }

        Type[] remainingSolution = solve(constraints, false, true);
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == null) solution[i] = remainingSolution[i];
        }
        return solution;
    }

    protected Type returnType(JL5ProcedureInstance pi) {
        if (pi instanceof MethodInstance) {
            return ((MethodInstance) pi).returnType();
        }
        return null;
    }

    private static boolean hasUnresolvedTypeArguments(Type[] solution) {
        for (Type element : solution) {
            if (element == null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public JL5TypeSystem typeSystem() {
        return ts;
    }

    public JL5ProcedureInstance procedureInstance() {
        return pi;
    }

}

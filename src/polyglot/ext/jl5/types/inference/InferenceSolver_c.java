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

    private JL5ProcedureInstance pi = null;

    private List<? extends Type> actualArgumentTypes;

    private List<? extends Type> formalTypes;

    private List<TypeVariable> typeVariablesToSolve;

    public InferenceSolver_c(JL5ProcedureInstance pi,
            List<? extends Type> actuals, JL5TypeSystem ts) {
        this.pi = pi;
        this.typeVariablesToSolve = pi.typeParams();
        this.actualArgumentTypes = actuals;
        this.formalTypes = pi.formalTypes();
        this.ts = ts;
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
        List<EqualConstraint> equals = new ArrayList<EqualConstraint>();
        List<SubTypeConstraint> subs = new ArrayList<SubTypeConstraint>();
        List<SuperTypeConstraint> supers = new ArrayList<SuperTypeConstraint>();
//        System.err.println("**** inference solver:");
//        System.err.println("      constraints : " + constraints);
//        System.err.println("      use subs? : " + useSubtypeConstraints +  "   use sups? : " + useSupertypeConstraints);
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
                return typeVariablesToSolve().indexOf(o1)
                        - typeVariablesToSolve().indexOf(o2);
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
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == null) {
                TypeVariable toSolve = typeVariablesToSolve().get(i);
                Set<ReferenceType> bounds = new LinkedHashSet<ReferenceType>();
                List<? extends Constraint> subSupConstraints =
                        useSubtypeConstraints ? subs : supers;
                for (Constraint c : subSupConstraints) {
                    if (c.formal.equals(toSolve) && c.actual.isReference()) {
                        bounds.add((ReferenceType) c.actual);
                    }
                }
                List<ReferenceType> u = new ArrayList<ReferenceType>(bounds);
                if (u.size() == 1) {
                    solution[i] = u.get(0);
                }
                else if (u.size() > 1) {
                    if (useSubtypeConstraints) {
                        solution[i] = ts.lub(Position.compilerGenerated(), u);
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
        List<Constraint> constraints = new ArrayList<Constraint>();
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
            // see if the return type is appropriate for inferring additional results
            Type returnType = returnType(pi);
            if (returnType != null && returnType.isReference()
                    && !returnType.isVoid()) {
                // See JLS 3rd ed, 15.12.2.8
                if (expectedReturnType == null) {
                    expectedReturnType = ts.Object();
                }
                solution =
                        solveWithExpectedReturnType(solution,
                                                    expectedReturnType,
                                                    returnType);
            }
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

                List<Constraint> cons = new ArrayList<Constraint>();
                cons.addAll(getInitialConstraints());
                cons.add(new SuperConversionConstraint(expectedReturnType,
                                                       returnType,
                                                       this));
                Type[] betterSolution = this.solve(cons, true, true);
                if (betterSolution != null) {
//                    System.err.println("Found a better solution: " + Arrays.asList(betterSolution));
                    solution = betterSolution;
                }
            }
        }

        Map<TypeVariable, ReferenceType> m =
                new LinkedHashMap<TypeVariable, ReferenceType>();
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == null) {
                // no solution for this variable.
                solution[i] = ts.Object();
            }
            m.put(typeVariablesToSolve().get(i), (ReferenceType) solution[i]);
        }
        return m;
    }

    private static Type returnType(JL5ProcedureInstance pi) {
        if (pi instanceof MethodInstance) {
            return ((MethodInstance) pi).returnType();
        }
        return null;
    }

    private Type[] solveWithExpectedReturnType(Type[] solution,
            Type expectedReturnType, Type declaredReturnType) {
        List<Constraint> constraints = new ArrayList<Constraint>();
        // get the subsitution corresponding to the solution so far
        Map<TypeVariable, ReferenceType> m =
                new LinkedHashMap<TypeVariable, ReferenceType>();
        for (int i = 0; i < solution.length; i++) {
            ReferenceType t = (ReferenceType) solution[i];
            if (t == null) {
                t = typeVariablesToSolve().get(i);
            }
            m.put(typeVariablesToSolve().get(i), t);
        }
        Subst<TypeVariable, ReferenceType> subst = ts.subst(m);
        Type rt = subst.substType(declaredReturnType);
        constraints.add(new SuperConversionConstraint(expectedReturnType,
                                                      rt,
                                                      this));
        for (int i = 0; i < solution.length; i++) {
            TypeVariable ti = typeVariablesToSolve().get(i);
            Type bi = subst.substType(ti.upperBound());
            constraints.add(new SuperConversionConstraint(bi, ti, this));
        }

        return solve(constraints, false, true);

    }

    private boolean hasUnresolvedTypeArguments(Type[] solution) {
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == null) {
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

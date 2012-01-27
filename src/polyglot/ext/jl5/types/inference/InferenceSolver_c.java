package polyglot.ext.jl5.types.inference;

import java.util.*;

import polyglot.ext.jl5.types.*;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.Position;

public class InferenceSolver_c implements InferenceSolver {

    private JL5TypeSystem ts;

    private JL5ProcedureInstance pi = null;

    private List<Type> actualArgumentTypes;

    private List<Type> formalTypes;

    private List<TypeVariable> typeVariablesToSolve;

    private Type expectedReturnType = null;

    public InferenceSolver_c(JL5ProcedureInstance pi, List<Type> actuals, JL5TypeSystem ts) {
        this.pi = pi;
        this.typeVariablesToSolve = pi.typeParams();
        this.actualArgumentTypes = actuals;
        this.formalTypes = pi.formalTypes();
        this.ts = ts;
    }

//    public InferenceSolver_c(List<TypeVariable> typeVars, List<Type> formals, List<Type> actuals,
//            JL5TypeSystem ts) {
//        this.ts = ts;
//        this.actualArgumentTypes = actuals;
//        this.formalTypes = formals;
//        this.typeVariablesToSolve = typeVars;
//    }

    public boolean isTargetTypeVariable(Type t) {
        if (t instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) t;
            return typeVariablesToSolve().contains(tv);
        }
        return false;
    }

    public List<TypeVariable> typeVariablesToSolve() {
        return typeVariablesToSolve;
    }

    public Map<TypeVariable, Type> solve() {
        List<Constraint> constraints = getInitialConstraints();
        List<EqualConstraint> equals = new ArrayList<EqualConstraint>();
        List<SubTypeConstraint> subs = new ArrayList<SubTypeConstraint>();
        List<SuperTypeConstraint> supers = new ArrayList<SuperTypeConstraint>();
//        System.err.println("**** inference solver:");
//        System.err.println("      constraints : " + constraints);

        while (!constraints.isEmpty()) {
            Constraint head = constraints.remove(0);
            if (head.canSimplify()) {
                constraints.addAll(0, head.simplify());
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
        Comparator<Constraint> comp = new Comparator<Constraint>() {
            public int compare(Constraint o1, Constraint o2) {
                return typeVariablesToSolve().indexOf(o1) - typeVariablesToSolve().indexOf(o2);
            }
        };
        Collections.sort(equals, comp);
        Collections.sort(subs, comp);

//        System.err.println("      equals : " + equals);
//        System.err.println("      subs   : " + subs);
//        System.err.println("      supers : " + supers);
        
        Type[] solution = new Type[typeVariablesToSolve().size()];
        for (EqualConstraint eq : equals) {
            int i = typeVariablesToSolve().indexOf(eq.formal);
            if ((solution[i] != null) && (!ts.equals(eq.actual, solution[i]))) {
                // incompatible equality constraints!
                // No solution.
                //solution[i] = ts.Object();
                return null;
            }
            else {
                solution[i] = eq.actual;
            }
        }
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == null) {
                TypeVariable toSolve = typeVariablesToSolve().get(i);
                Set<ReferenceType> uset = new HashSet<ReferenceType>();
                for (Constraint c : subs) {
                    if (c.formal.equals(toSolve))
                        uset.add((ReferenceType) c.actual);
                }
                List<ReferenceType> u = new ArrayList<ReferenceType>(uset);
                if (u.size() == 1) {
                    solution[i] = u.get(0);
                }
                else if (u.size() > 1) {
                    solution[i] = ts.intersectionType(Position.compilerGenerated(), u);
                }
            }
        }
        Map<TypeVariable, Type> m = new LinkedHashMap();
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == null) {
                solution[i] = ts.Object();
            }
            m.put(typeVariablesToSolve().get(i), solution[i]);
        }
        return m;
    }

    private List<Constraint> getInitialConstraints() {
        List<Constraint> constraints = new ArrayList<Constraint>();
        int numFormals = formalTypes.size();
        for (int i = 0; i < numFormals - 1; i++) {
            constraints.add(new SubConversionConstraint(actualArgumentTypes.get(i), formalTypes.get(i), this));
        }
        if (numFormals > 0) {
            if (pi != null && JL5Flags.isVarArgs(pi.flags())) {
                JL5ArrayType lastFormal = (JL5ArrayType) pi.formalTypes().get(numFormals - 1);
                for (int i = numFormals - 1; i < actualArgumentTypes.size() - 1; i++) {
                    constraints.add(new SubConversionConstraint(actualArgumentTypes.get(i), lastFormal.base(), this));
                }                
            } else if (numFormals == actualArgumentTypes.size()) {
                constraints.add(new SubConversionConstraint(actualArgumentTypes.get(numFormals - 1), formalTypes.get(numFormals - 1), this));
            }
        }
        return constraints;
    }

    public  Map<TypeVariable, Type> solve(Type expectedReturnType) {
        this.expectedReturnType = expectedReturnType;
        return solve();
    }

    public JL5TypeSystem typeSystem() {
        return ts;
    }

    public JL5ProcedureInstance procedureInstance() {
        return pi;
    }

}

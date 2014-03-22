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

    @Override
    public DiamondType diamondType(Position pos, JL5ParsedClassType base) {
        return new DiamondType_c(pos, base);
    }

    @Override
    public ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> typeArgs, ClassType currClass)
            throws SemanticException {
        return findConstructor(container, argTypes, typeArgs, currClass, null);
    }

    @Override
    public ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> typeArgs, ClassType currClass,
            Type expectedObjectType) throws SemanticException {
        assert_(container);
        assert_(argTypes);

        List<ConstructorInstance> acceptable =
                findAcceptableConstructors(container,
                                           argTypes,
                                           typeArgs,
                                           currClass,
                                           expectedObjectType);

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
            List<? extends ReferenceType> actualTypeArgs, ClassType currClass)
            throws SemanticException {
        return this.findAcceptableConstructors(container,
                                               argTypes,
                                               actualTypeArgs,
                                               currClass,
                                               null);
    }

    /**
     * Populates the list acceptable with those ConstructorInstances which are
     * Applicable and Accessible as defined by JLS 15.12.2
     * @throws SemanticException 
     */
    protected List<ConstructorInstance> findAcceptableConstructors(
            ClassType container, List<? extends Type> argTypes,
            List<? extends ReferenceType> actualTypeArgs, ClassType currClass,
            Type expectedObjectType) throws SemanticException {
        assert_(container);
        assert_(argTypes);

        // apply capture conversion to container and argTypes
        container =
                (ClassType) applyCaptureConversion(container,
                                                   container.position());
        List<Type> captureConvertedArgTypes =
                new ArrayList<Type>(argTypes.size());
        for (Type argType : argTypes) {
            if (argType instanceof ReferenceType)
                argType = applyCaptureConversion(argType, argType.position());
            captureConvertedArgTypes.add(argType);
        }
        argTypes = captureConvertedArgTypes;

        SemanticException error = null;

        // List of constructors accessible from curClass that have valid new
        // call without boxing/unboxing conversion or variable arity and
        // are not overridden by an unaccessible constructor
        List<ConstructorInstance> phase1constructors =
                new ArrayList<ConstructorInstance>();
        // List of constructors accessible from curClass that have a valid new
        // call relying on boxing/unboxing conversion
        List<ConstructorInstance> phase2constructors =
                new ArrayList<ConstructorInstance>();
        // List of constructors accessible from curClass that have a valid new
        // call relying on boxing/unboxing conversion and variable arity
        List<ConstructorInstance> phase3constructors =
                new ArrayList<ConstructorInstance>();

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
                if (isAccessible(ci, currClass)) {
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
    public JL5ProcedureInstance callValid(JL5ProcedureInstance mi,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> actualTypeArgs,
            Type expectedReturnType) {
        if (actualTypeArgs == null) {
            actualTypeArgs = Collections.emptyList();
        }

        // First check that the number of arguments is reasonable
        if (argTypes.size() != mi.formalTypes().size()) {
            // the actual args don't match the number of the formal args.
            if (!(mi.isVariableArity() && argTypes.size() >= mi.formalTypes()
                                                               .size() - 1)) {
                // the last (variable) argument can consume 0 or more of the actual arguments.
                return null;
            }

        }

        JL5Subst subst = null;
        if (!mi.typeParams().isEmpty() && actualTypeArgs.isEmpty()) {
            // need to perform type inference
            subst = inferTypeArgs(mi, argTypes, expectedReturnType);
        }
        else if (!mi.typeParams().isEmpty() && !actualTypeArgs.isEmpty()) {
            Map<TypeVariable, ReferenceType> m =
                    new HashMap<TypeVariable, ReferenceType>();
            Iterator<? extends ReferenceType> iter = actualTypeArgs.iterator();
            for (TypeVariable tv : mi.typeParams()) {
                m.put(tv, iter.next());
            }
            subst = (JL5Subst) this.subst(m);
        }

        JL5ProcedureInstance mj = mi;
        if (!mi.typeParams().isEmpty() && subst != null) {
            // check that the substitution satisfies the bounds

            for (TypeVariable tv : subst.substitutions().keySet()) {
                Type a = subst.substitutions().get(tv);
                if (!isSubtype(a, tv.upperBound())) {
                    return null;
                }
            }

            mj = subst.substProcedure(mi);
        }

        if (super.callValid(mj, argTypes)) {
            return mj;
        }

        return null;
    }
}

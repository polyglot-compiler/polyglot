package polyglot.ext.jl5.types;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polyglot.ext.param.types.Subst;
import polyglot.main.Report;
import polyglot.types.ArrayType;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.MethodInstance_c;
import polyglot.types.ProcedureInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;

@SuppressWarnings("serial")
public class JL5MethodInstance_c extends MethodInstance_c implements
        JL5MethodInstance {
    private List<TypeVariable> typeParams;
    private RetainedAnnotations retainedAnnotations;

    public JL5MethodInstance_c(JL5TypeSystem ts, Position pos,
            ReferenceType container, Flags flags, Type returnType, String name,
            List<? extends Type> argTypes, List<? extends Type> excTypes,
            List<TypeVariable> typeParams) {
        super(ts, pos, container, flags, returnType, name, argTypes, excTypes);
        this.typeParams = typeParams;
    }

    @Override
    public boolean isVariableArity() {
        return JL5Flags.isVarArgs(this.flags());
    }

    @Override
    public List<MethodInstance> overridesImpl() {
        List<MethodInstance> l = new LinkedList<MethodInstance>();
        ReferenceType rt = container();
        JL5TypeSystem ts = (JL5TypeSystem) this.typeSystem();
        while (rt != null) {
            // add any method with the same name and formalTypes from 
            // rt
            for (MethodInstance mj : rt.methodsNamed(name)) {
                if (ts.areOverrideEquivalent(this, (JL5MethodInstance) mj)) {
                    l.add(mj);
                }
            }

            ReferenceType sup = null;
            if (rt.superType() != null && rt.superType().isReference()) {
                sup = (ReferenceType) rt.superType();
            }

            rt = sup;
        }
        ;

        return l;
    }

    @Override
    public boolean canOverrideImpl(MethodInstance mj_, boolean quiet)
            throws SemanticException {
        JL5MethodInstance mi = this;
        if (!(mj_ instanceof JL5MethodInstance)) {
            return false;
        }
        JL5MethodInstance mj = (JL5MethodInstance) mj_;

        JL5TypeSystem ts = (JL5TypeSystem) this.typeSystem();
        if (!(ts.areOverrideEquivalent(mi, mj))) {
            if (quiet) return false;
            throw new SemanticException(mi.signature() + " in "
                    + mi.container() + " cannot override " + mj.signature()
                    + " in " + mj.container()
                    + "; incompatible parameter types", mi.position());
        }

        if (mi != mj && !mi.equals(mj) && mj.flags().isFinal()) {
            // mi can "override" a final method mj if mi and mj are the same method instance.
            if (Report.should_report(Report.types, 3))
                Report.report(3, mj.flags() + " final");
            if (quiet) return false;
            throw new SemanticException(mi.signature()
                                                + " in "
                                                + mi.container()
                                                + " cannot override "
                                                + mj.signature()
                                                + " in "
                                                + mj.container()
                                                + "; overridden method is final",
                                        mi.position());
        }

        // replace the type variables of mj with the type variables of mi
        if (!mi.typeParams().isEmpty()) {
            Map<TypeVariable, ReferenceType> substm =
                    new LinkedHashMap<TypeVariable, ReferenceType>();
            for (int i = 0; i < mi.typeParams().size(); i++) {
                substm.put(mj.typeParams().get(i), mi.typeParams().get(i));
            }
            Subst<TypeVariable, ReferenceType> subst = ts.subst(substm);
            mj = subst.substMethod(mj);
        }

        Type miRet = mi.returnType();
        Type mjRet = mj.returnType();

        if (!ts.areReturnTypeSubstitutable(miRet, mjRet)) {
            if (Report.should_report(Report.types, 3))
                Report.report(3, "return type " + miRet + " != " + mjRet);
            if (quiet) return false;
            throw new SemanticException(mi.signature()
                                                + " in "
                                                + mi.container()
                                                + " cannot override "
                                                + mj.signature()
                                                + " in "
                                                + mj.container()
                                                + "; attempting to use incompatible "
                                                + "return type\n" + "found: "
                                                + miRet + "\n" + "required: "
                                                + mjRet,
                                        mi.position());
        }

        if (!ts.throwsSubset(mi, mj)) {
            if (Report.should_report(Report.types, 3))
                Report.report(3,
                              mi.throwTypes() + " not subset of "
                                      + mj.throwTypes());
            if (quiet) return false;
            throw new SemanticException(mi.signature()
                                                + " in "
                                                + mi.container()
                                                + " cannot override "
                                                + mj.signature()
                                                + " in "
                                                + mj.container()
                                                + "; the throw set "
                                                + mi.throwTypes()
                                                + " is not a subset of the "
                                                + "overridden method's throw set "
                                                + mj.throwTypes() + ".",
                                        mi.position());
        }

        if (mi.flags().moreRestrictiveThan(mj.flags())) {
            if (Report.should_report(Report.types, 3))
                Report.report(3,
                              mi.flags() + " more restrictive than "
                                      + mj.flags());
            if (quiet) return false;
            throw new SemanticException(mi.signature()
                                                + " in "
                                                + mi.container()
                                                + " cannot override "
                                                + mj.signature()
                                                + " in "
                                                + mj.container()
                                                + "; attempting to assign weaker "
                                                + "access privileges",
                                        mi.position());
        }

        if (mi.flags().isStatic() != mj.flags().isStatic()) {
            if (Report.should_report(Report.types, 3))
                Report.report(3, mi.signature() + " is "
                        + (mi.flags().isStatic() ? "" : "not") + " static but "
                        + mj.signature() + " is "
                        + (mj.flags().isStatic() ? "" : "not") + " static");
            if (quiet) return false;
            throw new SemanticException(mi.signature()
                                                + " in "
                                                + mi.container()
                                                + " cannot override "
                                                + mj.signature()
                                                + " in "
                                                + mj.container()
                                                + "; overridden method is "
                                                + (mj.flags().isStatic() ? ""
                                                        : "not") + "static",
                                        mi.position());
        }

        return true;
    }

    @Override
    public boolean callValidImpl(List<? extends Type> argTypes) {
        List<Type> myFormalTypes = this.formalTypes;

        //         System.err.println("JL5MethodInstance_c callValid Impl " + this +" called with " +argTypes);
        // now compare myFormalTypes to argTypes
        if (!this.isVariableArity() && argTypes.size() != myFormalTypes.size()) {
            //            System.err.println("     1");
            return false;
        }
        if (this.isVariableArity()
                && argTypes.size() < myFormalTypes.size() - 1) {
            // the last (variable) argument can consume 0 or more of the actual arguments. 
            //            System.err.println("     2");
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
            if (!formalTypes.hasNext() && this.isVariableArity()) {
                // varible arity method, and this is the last arg.
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
            if (this.isVariableArity()
                    && myFormalTypes.size() == argTypes.size()
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
                    //                         System.err.println("     3: failed " + actual + " to " +formal + " and " + actual + " to " + arr);
                    return false;
                }
            }
            else {
                //                     System.err.println("     4: failed " + actual + " to " +formal);
                return false;
            }
        }

        return true;
    }

    /**
     * See JLS 3rd ed. 15.12.2.5.
     */
    @Override
    public boolean moreSpecificImpl(ProcedureInstance p) {
        JL5MethodInstance p1 = this;
        JL5MethodInstance p2 = (JL5MethodInstance) p;

        return ts.callValid(p2, p1.formalTypes());

    }

    @Override
    public boolean isCanonical() {
        return super.isCanonical() && listIsCanonical(typeParams);
    }

    @Override
    public void setTypeParams(List<TypeVariable> typeParams) {
        this.typeParams = typeParams;
    }

    @Override
    public List<TypeVariable> typeParams() {
        return Collections.unmodifiableList(this.typeParams);
    }

    @Override
    public JL5Subst erasureSubst() {
        JL5TypeSystem ts = (JL5TypeSystem) this.typeSystem();
        return ts.erasureSubst(this);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(designator());
        sb.append(" ");
        if (!this.typeParams.isEmpty()) {
            sb.append("<");
            Iterator<TypeVariable> iter = this.typeParams().iterator();
            while (iter.hasNext()) {
                sb.append(iter.next());
                if (iter.hasNext()) {
                    sb.append(",");
                }
            }
            sb.append(">");
        }
        sb.append(flags.translate());
        sb.append(returnType);
        sb.append(" ");
        sb.append(container());
        sb.append(" ");
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
    public RetainedAnnotations retainedAnnotations() {
        return this.retainedAnnotations;
    }

    @Override
    public void setRetainedAnnotations(RetainedAnnotations retainedAnnotations) {
        this.retainedAnnotations = retainedAnnotations;
    }
}

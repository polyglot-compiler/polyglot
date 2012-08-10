package polyglot.ext.jl5.types;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance_c;
import polyglot.types.Flags;
import polyglot.types.Type;
import polyglot.util.Position;

@SuppressWarnings("serial")
public class JL5ConstructorInstance_c extends ConstructorInstance_c implements
        JL5ConstructorInstance {
    private List<TypeVariable> typeParams;
    private RetainedAnnotations retainedAnnotations;

    public JL5ConstructorInstance_c(JL5TypeSystem_c ts, Position pos,
            ClassType container, Flags flags, List<? extends Type> argTypes,
            List<? extends Type> excTypes, List<TypeVariable> typeParams) {
        super(ts, pos, container, flags, argTypes, excTypes);
        this.typeParams = typeParams;
    }

    @Override
    public boolean isVariableArity() {
        return JL5Flags.isVarArgs(this.flags());
    }

    @Override
    public boolean callValidImpl(List<? extends Type> argTypes) {
        List<Type> myFormalTypes = this.formalTypes;

        // System.err.println("JL5MethodInstance_c callValid Impl " + this +
        // " called with " +argTypes);
        // now compare myFormalTypes to argTypes
        if (!this.isVariableArity() && argTypes.size() != myFormalTypes.size()) {
            return false;
        }
        if (this.isVariableArity()
                && argTypes.size() < myFormalTypes.size() - 1) {
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
    public RetainedAnnotations retainedAnnotations() {
        return this.retainedAnnotations;
    }

    @Override
    public void setRetainedAnnotations(RetainedAnnotations retainedAnnotations) {
        this.retainedAnnotations = retainedAnnotations;
    }

}

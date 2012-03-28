package polyglot.ext.jl5.types;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.types.*;
import polyglot.util.Position;

@SuppressWarnings("serial")
public class JL5ConstructorInstance_c extends ConstructorInstance_c implements JL5ConstructorInstance {
    private List<TypeVariable> typeParams;
    public JL5ConstructorInstance_c(JL5TypeSystem_c ts,
			Position pos, ClassType container, Flags flags, List argTypes,
			List excTypes, List typeParams) {
    	super(ts, pos, container, flags, argTypes, excTypes);
    	this.typeParams = typeParams;
	}

	public boolean isVariableArity() {
        return JL5Flags.isVarArgs(this.flags());
    }
	
	@Override
    public boolean callValidImpl(List argTypes) {
        List<Type> myFormalTypes = this.formalTypes;
        JL5Subst erasureSubst = null;
        if (this.container() instanceof JL5ParsedClassType) {
            // we have a stripped off class type. Replace any type variables
            // with their bounds.
            erasureSubst = ((JL5ParsedClassType) this.container()).erasureSubst();
        }

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
        Iterator formalTypes = myFormalTypes.iterator();
        Iterator actualTypes = argTypes.iterator();
        Type formal = null;
        while (actualTypes.hasNext()) {
            Type actual = (Type) actualTypes.next();
            if (formalTypes.hasNext()) {
                formal = (Type) formalTypes.next();
            }
            if (!formalTypes.hasNext() && this.isVariableArity()) {
                // varible arity method, and this is the last arg.
                ArrayType arr = (ArrayType) myFormalTypes.get(myFormalTypes
                        .size() - 1);
                formal = arr.base();
            }
            if (ts.isImplicitCastValid(actual, formal)) {
                return true;
            }
            if (erasureSubst != null && ts.isImplicitCastValid(actual, erasureSubst.substType(formal))) {
                return true;
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
                ArrayType arr = (ArrayType) myFormalTypes.get(myFormalTypes
                                                              .size() - 1);
                if (!ts.isImplicitCastValid(actual, arr)) {
                    // System.err.println("     3: failed " + actual +
                    // " to " +formal + " and " + actual + " to " + arr);
                    return false;
                }
            } else {
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
    public boolean isRawGeneric() {
        return !this.typeParams.isEmpty();
    }

    @Override
    public boolean isInstantiatedGeneric() {
        return false;
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
        return ts.erasureSubst(this.typeParams);
    }
}

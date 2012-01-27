package polyglot.ext.jl5.types;

import polyglot.types.Context;
import polyglot.types.Type;
import polyglot.types.VarInstance;
public interface JL5Context extends Context {

    VarInstance findVariableInThisScope(String name);
    VarInstance findVariableSilent(String name);

    JL5Context pushTypeVariable(TypeVariable iType);
    TypeVariable findTypeVariableInThisScope(String name);

    boolean inTypeVariable();

    JL5Context addTypeVariable(TypeVariable type);

    JL5TypeSystem typeSystem();
	Context pushSwitch(Type type);
	Type switchType();
//	public Context pushCase();
//	boolean inCase();
}

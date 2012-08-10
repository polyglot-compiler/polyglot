package polyglot.ext.jl5.types;

import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.Type;
import polyglot.types.VarInstance;

public interface JL5Context extends Context {

    VarInstance findVariableInThisScope(String name);

    @Override
    VarInstance findVariableSilent(String name);

    JL5Context pushTypeVariable(TypeVariable iType);

    TypeVariable findTypeVariableInThisScope(String name);

    boolean inTypeVariable();

    void addTypeVariable(TypeVariable type);

    @Override
    JL5TypeSystem typeSystem();

    Context pushSwitch(Type type);

    Type switchType();

    Context pushExtendsClause(ClassType declaringClass);

    boolean inExtendsClause();

    ClassType extendsClauseDeclaringClass();

    Context pushCTORCall();

    boolean inCTORCall();
//	public Context pushCase();
//	boolean inCase();
}

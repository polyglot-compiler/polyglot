package polyglot.ext.jl7.types;

import java.util.List;

import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5ProcedureInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;

public interface JL7TypeSystem extends JL5TypeSystem {

    DiamondType diamondType(Position pos, JL5ParsedClassType base);

    ConstructorInstance findConstructor(ClassType container,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> typeArgs, ClassType currClass,
            Type expectedObjectType) throws SemanticException;

    JL5ProcedureInstance callValid(JL5ProcedureInstance mi,
            List<? extends Type> argTypes,
            List<? extends ReferenceType> actualTypeArgs,
            Type expectedReturnType);
}

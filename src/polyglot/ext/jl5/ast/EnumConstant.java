package polyglot.ext.jl5.ast;

import polyglot.ast.Field;
import polyglot.ext.jl5.types.EnumInstance;

public interface EnumConstant extends Field {
    EnumInstance enumInstance();

    EnumConstant enumInstance(EnumInstance enumInstance);
}

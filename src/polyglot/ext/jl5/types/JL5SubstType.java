package polyglot.ext.jl5.types;

import polyglot.ext.param.types.InstType;
import polyglot.ext.param.types.SubstType;
import polyglot.types.ReferenceType;

public interface JL5SubstType extends SubstType<TypeVariable, ReferenceType>,
        InstType<TypeVariable, ReferenceType>, JL5ClassType {

}

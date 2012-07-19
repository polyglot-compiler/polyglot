package polyglot.ext.jl5.types;

import polyglot.types.TypeSystem;
import polyglot.util.Position;

public class UnknownTypeVariable_c extends TypeVariable_c implements
        UnknownTypeVariable {

    public UnknownTypeVariable_c(TypeSystem ts) {
        super(ts, Position.COMPILER_GENERATED, "<unknown>", null);
    }

}

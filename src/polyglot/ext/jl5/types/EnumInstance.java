package polyglot.ext.jl5.types;

import polyglot.types.MemberInstance;

public interface EnumInstance extends JL5FieldInstance, MemberInstance {
    long ordinal();
}

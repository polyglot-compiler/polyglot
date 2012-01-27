package polyglot.ext.jl5.types;

import polyglot.types.FieldInstance;
import polyglot.types.MemberInstance;

public interface EnumInstance extends FieldInstance, MemberInstance {
	long ordinal();
}


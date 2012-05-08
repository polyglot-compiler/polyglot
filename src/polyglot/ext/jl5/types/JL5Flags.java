package polyglot.ext.jl5.types;

import polyglot.types.Flags;

/**
 * Utility methods for manipulating JL5 Flags.
 */
public abstract class JL5Flags extends Flags {

	public static final int ENUM_MOD = 0x00004000; // java.lang.reflect.Modifier.ENUM;
	public static final int VARARGS_MOD = 0x00000080; // java.lang.reflect.Modifier.VARARGS;
	public static final Flags ENUM = createFlag("enum", null);
	public static final Flags VARARGS = createFlag("varargs", null);

	private JL5Flags() {
		super();
	}

	public static Flags setEnum(Flags f) {
		return f.set(ENUM);
	}

	public static Flags clearEnum(Flags f) {
		return f.clear(ENUM);
	}

	public static boolean isEnum(Flags f) {
		return f.contains(ENUM);
	}

	public static Flags setVarArgs(Flags f) {
		return f.set(VARARGS);
	}

	public static Flags clearVarArgs(Flags f) {
		return f.clear(VARARGS);
	}

	public static boolean isVarArgs(Flags f) {
		return f.contains(VARARGS);
	}

}

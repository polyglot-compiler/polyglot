package jltools.ext.jl.types;

import jltools.types.*;
import jltools.util.InternalCompilerError;
import java.util.*;

/**
 * An <code>PrimitiveType_c</code> represents a primitive type.
 */
public class PrimitiveType_c extends Type_c implements PrimitiveType
{
	protected Kind kind;

	/** Used for deserializing types. */
	protected PrimitiveType_c() { }

	public PrimitiveType_c(TypeSystem ts, Kind kind) {
		super(ts);
		this.kind = kind;
	}

	public Kind kind() {
		return kind;
	}

	public String toString() {
		return kind.toString();
	}

	public String translate(Resolver c) {
		return ts.translatePrimitive(c, this);
	}

	public boolean isCanonical() { return true; }
	public boolean isPrimitive() { return true; }
	public PrimitiveType toPrimitive() { return this; }

	public boolean isVoid() { return kind == VOID; }
	public boolean isBoolean() { return kind == BOOLEAN; }
	public boolean isChar() { return kind == CHAR; }
	public boolean isByte() { return kind == BYTE; }
	public boolean isShort() { return kind == SHORT; }
	public boolean isInt() { return kind == INT; }
	public boolean isLong() { return kind == LONG; }
	public boolean isFloat() { return kind == FLOAT; }
	public boolean isDouble() { return kind == DOUBLE; }
	public boolean isIntOrLess() { return kind == CHAR || kind == BYTE || kind == SHORT || kind == INT; }
	public boolean isLongOrLess() { return isIntOrLess() || kind == LONG; }
	public boolean isNumeric() { return isLongOrLess() || kind == FLOAT || kind == DOUBLE; }

	public int hashCode() {
		return kind.hashCode();
	}

	public boolean equals(Object o) {
		if (o instanceof PrimitiveType) {
			PrimitiveType t = (PrimitiveType) o;
			return kind == t.kind();
		}

		return false;
	}

	public String wrapperTypeString() {
		return ts.wrapperTypeString(this);
	}
	
	public String name() {
		return toString();	
	}
	
	public String fullName() {
		return name();
	}
}

package polyglot.ext.jl.types;

import polyglot.types.*;
import polyglot.util.InternalCompilerError;
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
            return kind.toString();
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

    public boolean isSameImpl(Type t) {
            return t.isPrimitive() && kind() == t.toPrimitive().kind();
    }

    public String wrapperTypeString(TypeSystem ts) {
            return ts.wrapperTypeString(this);
    }
    
    public String name() {
            return toString();	
    }
    
    public String fullName() {
            return name();
    }

    public boolean descendsFromImpl(Type ancestor) {
        return false;
    }

    public boolean isImplicitCastValidImpl(Type toType) {
        if (! toType.isPrimitive()) return false;

        PrimitiveType t = toType.toPrimitive();
        PrimitiveType f = this;

        if (t.isVoid()) return false;
        if (f.isVoid()) return false;

        if (ts.isSame(t, f)) return true;

        if (t.isBoolean()) return f.isBoolean();
        if (f.isBoolean()) return false;

        if (! f.isNumeric() || ! t.isNumeric()) return false;

        if (t.isDouble()) return true;
        if (f.isDouble()) return false;

        if (t.isFloat()) return true;
        if (f.isFloat()) return false;

        if (t.isLong()) return true;
        if (f.isLong()) return false;

        if (t.isInt()) return true;
        if (f.isInt()) return false;

        if (t.isShort()) return f.isShort() || f.isByte();
        if (f.isShort()) return false;

        if (t.isChar()) return f.isChar();
        if (f.isChar()) return false;

        if (t.isByte()) return f.isByte();
        if (f.isByte()) return false;

        return false;
    }

    /**
     * Requires: all type arguments are canonical.  ToType is not a NullType.
     *
     * Returns true iff a cast from this to toType is valid; in other
     * words, some non-null members of this are also members of toType.
     **/
    public boolean isCastValidImpl(Type toType) {
	if (isVoid() || toType.isVoid()) return false;
        if (ts.isSame(this, toType)) return true;
	if (isNumeric() && toType.isNumeric()) return true;
        return false;
    }

    public boolean numericConversionValidImpl(long value) {
        if (isByte())
            return Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE;
        if (isShort())
            return Short.MIN_VALUE <= value && value <= Short.MAX_VALUE;
        if (isChar())
            return Character.MIN_VALUE <= value && value <= Character.MAX_VALUE;
        if (isInt())
            return Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE;
        if (isLong())
            return true;
        return false;
    }
}

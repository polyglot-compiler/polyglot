package polyglot.types;

import polyglot.util.InternalCompilerError;
import polyglot.util.Copy;

import java.lang.reflect.Modifier;
import java.io.Serializable;

/**
 * <code>Flags</code> is an immutable set of class, method, or field modifiers.
 * We represent package scope as the abscence of private, public and protected
 * scope modifiers.
 */
public class Flags implements Copy, Serializable
{
    public static final Flags NONE         = new Flags();

    public static final Flags PUBLIC       = new Flags(Modifier.PUBLIC);
    public static final Flags PROTECTED    = new Flags(Modifier.PROTECTED);
    public static final Flags PRIVATE      = new Flags(Modifier.PRIVATE);
    public static final Flags STATIC       = new Flags(Modifier.STATIC);
    public static final Flags FINAL        = new Flags(Modifier.FINAL);
    public static final Flags SYNCHRONIZED = new Flags(Modifier.SYNCHRONIZED);
    public static final Flags TRANSIENT    = new Flags(Modifier.TRANSIENT);
    public static final Flags NATIVE       = new Flags(Modifier.NATIVE);
    public static final Flags INTERFACE    = new Flags(Modifier.INTERFACE);
    public static final Flags ABSTRACT     = new Flags(Modifier.ABSTRACT);
    public static final Flags VOLATILE     = new Flags(Modifier.VOLATILE);
    public static final Flags STRICTFP     = new Flags(Modifier.STRICT);

    protected int bits;

    /**
     * Effects: returns a new accessflags object with no accessflags set.
     */
    public Flags() {
	this(0);
    }

    /**
     * Given the JVM encoding of a set of flags, returns the Flags object
     * for that encoding.
     */
    public Flags(int bits) {
	this.bits = bits;
    }

    /**
     * Returns a copy of this.
     */
    public Object copy() {
        return copyFlags();
    }

    private Flags copyFlags() {
        try {
	    return (Flags) clone();
	}
	catch (CloneNotSupportedException e) {
	    throw new InternalCompilerError("clone() not supported");
	}
    }

    public Flags set(Flags other) {
	return copyFlags().set_bits(other);
    }

    public Flags clear(Flags other) {
	return copyFlags().clear_bits(other);
    }

    public Flags retain(Flags other) {
        Flags a = copyFlags();
	a.bits &= other.bits;
	return a;
    }

    public Flags clear() {
        return new Flags();
    }

    private boolean is_set(Flags other) {
	return (bits & other.bits) != 0;
    }

    public boolean contains(Flags other) {
	return (bits & other.bits) == other.bits
	    && (bits | other.bits) == bits;
    }

    private Flags set_bits(Flags other) {
	bits |= other.bits;
	return this;
    }

    private Flags clear_bits(Flags other) {
	bits &= ~other.bits;
	return this;
    }

    public Flags setPublic() {
	return copyFlags().set_bits(PUBLIC);
    }

    public Flags clearPublic() {
	return copyFlags().clear_bits(PUBLIC);
    }

    public boolean isPublic() {
	return is_set(PUBLIC);
    }

    public Flags setPrivate() {
	return copyFlags().set_bits(PRIVATE);
    }

    public Flags clearPrivate() {
	return copyFlags().clear_bits(PRIVATE);
    }

    public boolean isPrivate() {
	return is_set(PRIVATE);
    }

    public Flags setProtected() {
	return copyFlags().set_bits(PROTECTED);
    }

    public Flags clearProtected() {
	return copyFlags().clear_bits(PROTECTED);
    }

    public boolean isProtected() {
	return is_set(PROTECTED);
    }

    public Flags setPackage() {
	return clear(PUBLIC).clear(PROTECTED).clear(PRIVATE);
    }

    public boolean isPackage() {
	return ! is_set(PUBLIC) && ! is_set(PROTECTED) && ! is_set(PRIVATE);
    }

    public Flags setStatic() {
	return copyFlags().set_bits(STATIC);
    }

    public Flags clearStatic() {
	return copyFlags().clear_bits(STATIC);
    }

    public boolean isStatic() {
	return is_set(STATIC);
    }

    public Flags setFinal() {
	return copyFlags().set_bits(FINAL);
    }

    public Flags clearFinal() {
	return copyFlags().clear_bits(FINAL);
    }

    public boolean isFinal() {
	return is_set(FINAL);
    }

    public Flags setSynchronized() {
	return copyFlags().set_bits(SYNCHRONIZED);
    }

    public Flags clearSynchronized() {
	return copyFlags().clear_bits(SYNCHRONIZED);
    }

    public boolean isSynchronized() {
	return is_set(SYNCHRONIZED);
    }

    public Flags setTransient() {
	return copyFlags().set_bits(TRANSIENT);
    }

    public Flags clearTransient() {
	return copyFlags().clear_bits(TRANSIENT);
    }

    public boolean isTransient() {
	return is_set(TRANSIENT);
    }

    public Flags setNative() {
	return copyFlags().set_bits(NATIVE);
    }

    public Flags clearNative() {
	return copyFlags().clear_bits(NATIVE);
    }

    public boolean isNative() {
	return is_set(NATIVE);
    }

    public Flags setInterface() {
	return copyFlags().set_bits(INTERFACE);
    }

    public Flags clearInterface() {
	return copyFlags().clear_bits(INTERFACE);
    }

    public boolean isInterface() {
	return is_set(INTERFACE);
    }

    public Flags setAbstract() {
	return copyFlags().set_bits(ABSTRACT);
    }

    public Flags clearAbstract() {
	return copyFlags().clear_bits(ABSTRACT);
    }

    public boolean isAbstract() {
	return is_set(ABSTRACT);
    }

    public Flags setVolatile() {
	return copyFlags().set_bits(VOLATILE);
    }

    public Flags clearVolatile() {
	return copyFlags().clear_bits(VOLATILE);
    }

    public boolean isVolatile() {
	return is_set(VOLATILE);
    }

    public Flags setStrictFP() {
	return copyFlags().set_bits(STRICTFP);
    }

    public Flags clearStrictFP() {
	return copyFlags().clear_bits(STRICTFP);
    }

    public boolean isStrictFP() {
	return is_set(STRICTFP);
    }

    public boolean moreRestrictiveThan(Flags f) {
        if (isPrivate() && (f.isProtected() || f.isPackage() || f.isPublic())) {
            return true;
        }

        if (isProtected() && (f.isPackage() || f.isPublic())) {
            return true;
        }

        if (isPackage() && f.isPublic()) {
            return true;
        }

        return false;
    }

    public String toString() {
        String s = "";
        if (isPublic())       s += (s.equals("") ? "" : " ") + "public";
        if (isPrivate())      s += (s.equals("") ? "" : " ") + "private";
        if (isProtected())    s += (s.equals("") ? "" : " ") + "protected";
        if (isStatic())       s += (s.equals("") ? "" : " ") + "static";
        if (isFinal())        s += (s.equals("") ? "" : " ") + "final";
        if (isSynchronized()) s += (s.equals("") ? "" : " ") + "synchronized";
        if (isTransient())    s += (s.equals("") ? "" : " ") + "transient";
        if (isNative())       s += (s.equals("") ? "" : " ") + "native";
        if (isInterface())    s += (s.equals("") ? "" : " ") + "interface";
        if (isAbstract())     s += (s.equals("") ? "" : " ") + "abstract";
        if (isVolatile())     s += (s.equals("") ? "" : " ") + "volatile";
        if (isStrictFP())     s += (s.equals("") ? "" : " ") + "strictfp";
        return s;
    }

    public String translate() {
        String s = toString();

	if (! s.equals("")) {
	    return s + " ";
	}

	return "";
    }

    public int hashCode() {
	return bits * 37;
    }

    public boolean equals(Object o) {
	return o instanceof Flags && bits == ((Flags) o).bits;
    }
}

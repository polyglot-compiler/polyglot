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

    /**
     * Returns a copy of this.
     */
    private Flags copyFlags() {
        try {
	    return (Flags) clone();
	}
	catch (CloneNotSupportedException e) {
	    throw new InternalCompilerError("clone() not supported");
	}
    }

    /**
     * Create new flags with the flags in <code>other</code> also set.
     */
    public Flags set(Flags other) {
	return copyFlags().set_bits(other);
    }

    /**
     * Create new flags with the flags in <code>other</code> cleared.
     */
    public Flags clear(Flags other) {
	return copyFlags().clear_bits(other);
    }

    /**
     * Create new flags with only flags in <code>other</code> set.
     */
    public Flags retain(Flags other) {
        Flags a = copyFlags();
	a.bits &= other.bits;
	return a;
    }

    /**
     * Return an empty set of flags.
     */
    public Flags clear() {
        return new Flags();
    }

    /**
     * Check if <i>any</i> flags in <code>other</code> are set.
     */
    private boolean is_set(Flags other) {
	return (bits & other.bits) != 0;
    }

    /**
     * Check if <i>all</i> flags in <code>other</code> are set.
     */
    public boolean contains(Flags other) {
	return (bits & other.bits) == other.bits
	    && (bits | other.bits) == bits;
    }

    /**
     * Destructively set the flags from <code>other</code>.
     */
    private Flags set_bits(Flags other) {
	bits |= other.bits;
	return this;
    }

    /**
     * Destructively clear the flags in <code>other</code>.
     */
    private Flags clear_bits(Flags other) {
	bits &= ~other.bits;
	return this;
    }

    /**
     * Return a copy of this <code>this</code> with the <code>public</code>
     * flag set.
     */
    public Flags setPublic() {
	return copyFlags().set_bits(PUBLIC);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>public</code>
     * flag clear.
     */
    public Flags clearPublic() {
	return copyFlags().clear_bits(PUBLIC);
    }

    /**
     * Return true if <code>this</code> has the <code>public</code> flag set.
     */
    public boolean isPublic() {
	return is_set(PUBLIC);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>private</code>
     * flag set.
     */
    public Flags setPrivate() {
	return copyFlags().set_bits(PRIVATE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>private</code>
     * flag clear.
     */
    public Flags clearPrivate() {
	return copyFlags().clear_bits(PRIVATE);
    }

    /**
     * Return true if <code>this</code> has the <code>private</code> flag set.
     */
    public boolean isPrivate() {
	return is_set(PRIVATE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>protected</code>
     * flag set.
     */
    public Flags setProtected() {
	return copyFlags().set_bits(PROTECTED);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>protected</code>
     * flag clear.
     */
    public Flags clearProtected() {
	return copyFlags().clear_bits(PROTECTED);
    }

    /**
     * Return true if <code>this</code> has the <code>protected</code> flag set.
     */
    public boolean isProtected() {
	return is_set(PROTECTED);
    }

    /**
     * Return a copy of this <code>this</code> with no access flags
     * (<code>public</code>, <code>private</code>, <code>protected</code>) set.
     */
    public Flags setPackage() {
	return clear(PUBLIC).clear(PROTECTED).clear(PRIVATE);
    }

    /**
     * Return true if <code>this</code> has the no access flags
     * (<code>public</code>, <code>private</code>, <code>protected</code>) set.
     */
    public boolean isPackage() {
	return ! is_set(PUBLIC) && ! is_set(PROTECTED) && ! is_set(PRIVATE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>static</code>
     * flag set.
     */
    public Flags setStatic() {
	return copyFlags().set_bits(STATIC);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>static</code>
     * flag clear.
     */
    public Flags clearStatic() {
	return copyFlags().clear_bits(STATIC);
    }

    /**
     * Return true if <code>this</code> has the <code>static</code> flag set.
     */
    public boolean isStatic() {
	return is_set(STATIC);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>final</code>
     * flag set.
     */
    public Flags setFinal() {
	return copyFlags().set_bits(FINAL);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>final</code>
     * flag clear.
     */
    public Flags clearFinal() {
	return copyFlags().clear_bits(FINAL);
    }

    /**
     * Return true if <code>this</code> has the <code>final</code> flag set.
     */
    public boolean isFinal() {
	return is_set(FINAL);
    }

    /**
     * Return a copy of this <code>this</code> with the
     * <code>synchronized</code> flag set.
     */
    public Flags setSynchronized() {
	return copyFlags().set_bits(SYNCHRONIZED);
    }

    /**
     * Return a copy of this <code>this</code> with the
     * <code>synchronized</code> flag clear.
     */
    public Flags clearSynchronized() {
	return copyFlags().clear_bits(SYNCHRONIZED);
    }

    /**
     * Return true if <code>this</code> has the <code>synchronized</code> flag
     * set.
     */
    public boolean isSynchronized() {
	return is_set(SYNCHRONIZED);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>transient</code>
     * flag set.
     */
    public Flags setTransient() {
	return copyFlags().set_bits(TRANSIENT);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>transient</code>
     * flag clear.
     */
    public Flags clearTransient() {
	return copyFlags().clear_bits(TRANSIENT);
    }

    /**
     * Return true if <code>this</code> has the <code>transient</code> flag set.
     */
    public boolean isTransient() {
	return is_set(TRANSIENT);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>native</code>
     * flag set.
     */
    public Flags setNative() {
	return copyFlags().set_bits(NATIVE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>native</code>
     * flag clear.
     */
    public Flags clearNative() {
	return copyFlags().clear_bits(NATIVE);
    }

    /**
     * Return true if <code>this</code> has the <code>native</code> flag set.
     */
    public boolean isNative() {
	return is_set(NATIVE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>interface</code>
     * flag set.
     */
    public Flags setInterface() {
	return copyFlags().set_bits(INTERFACE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>interface</code>
     * flag clear.
     */
    public Flags clearInterface() {
	return copyFlags().clear_bits(INTERFACE);
    }

    /**
     * Return true if <code>this</code> has the <code>interface</code> flag set.
     */
    public boolean isInterface() {
	return is_set(INTERFACE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>abstract</code>
     * flag set.
     */
    public Flags setAbstract() {
	return copyFlags().set_bits(ABSTRACT);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>abstract</code>
     * flag clear.
     */
    public Flags clearAbstract() {
	return copyFlags().clear_bits(ABSTRACT);
    }

    /**
     * Return true if <code>this</code> has the <code>abstract</code> flag set.
     */
    public boolean isAbstract() {
	return is_set(ABSTRACT);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>volatile</code>
     * flag set.
     */
    public Flags setVolatile() {
	return copyFlags().set_bits(VOLATILE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>volatile</code>
     * flag clear.
     */
    public Flags clearVolatile() {
	return copyFlags().clear_bits(VOLATILE);
    }

    /**
     * Return true if <code>this</code> has the <code>volatile</code> flag set.
     */
    public boolean isVolatile() {
	return is_set(VOLATILE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>strictfp</code>
     * flag set.
     */
    public Flags setStrictFP() {
	return copyFlags().set_bits(STRICTFP);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>strictfp</code>
     * flag clear.
     */
    public Flags clearStrictFP() {
	return copyFlags().clear_bits(STRICTFP);
    }

    /**
     * Return true if <code>this</code> has the <code>strictfp</code> flag set.
     */
    public boolean isStrictFP() {
	return is_set(STRICTFP);
    }

    /**
     * Return true if <code>this</code> has more restrictive access flags than
     * <code>f</code>.
     */
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

    /**
     * Return "" if no flags set, or toString() + " " if some flags are set.
     */
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

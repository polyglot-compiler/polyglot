package polyglot.types;

import polyglot.util.InternalCompilerError;
import polyglot.util.Enum;

import java.io.Serializable;
import java.util.*;

/**
 * <code>Flags</code> is an immutable set of class, method, or field modifiers.
 * We represent package scope as the absence of private, public and protected
 * scope modifiers.
 */
public class Flags implements Serializable
{
    protected static ArrayList all_flags = new ArrayList();

    protected static class Flag extends Enum {
        protected Flag(String name) {
            super(name);
        }
    }

    public static final Flags NONE         = new Flags();
    public static final Flags PUBLIC       = createFlag("public");
    public static final Flags PRIVATE      = createFlag("private");
    public static final Flags PROTECTED    = createFlag("protected");
    public static final Flags STATIC       = createFlag("static");
    public static final Flags FINAL        = createFlag("final");
    public static final Flags SYNCHRONIZED = createFlag("synchronized");
    public static final Flags TRANSIENT    = createFlag("transient");
    public static final Flags NATIVE       = createFlag("native");
    public static final Flags INTERFACE    = createFlag("interface");
    public static final Flags ABSTRACT     = createFlag("abstract");
    public static final Flags VOLATILE     = createFlag("volatile");
    public static final Flags STRICTFP     = createFlag("strictfp");

    /** All access flags. */
    protected static final Flags ACCESS_FLAGS = PUBLIC.Private().Protected();

    protected Set flags;

    /**
     * Return a new Flags object with a new name.  Should only be called once
     * per name.
     */
    public static Flags createFlag(String name) {
        Flag f = new Flag(name);
        all_flags.add(f);
        return new Flags(f);
    }

    public static Flags createFlag(String name, Flags after) {
        Flag f = new Flag(name);

        // Insert in "all_flags" just after the last flag in "after".
        for (int i = all_flags.size()-1; i >= 0; i--) {
            Object o = all_flags.get(i);

            if (after.flags.contains(o)) {
                all_flags.add(i+1, f);
                return new Flags(f);
            }
        }

        all_flags.add(0, f);

        return new Flags(f);
    }

    /**
     * Effects: returns a new accessflags object with no accessflags set.
     */
    public Flags() {
        flags = new HashSet();
    }

    /**
     * Given the name  encoding of a set of flags, returns the Flags object
     * for that encoding.
     */
    public Flags(Flag flag) {
        flags = new HashSet();
        flags.add(flag);
    }

    /**
     * Given the name  encoding of a set of flags, returns the Flags object
     * for that encoding.
     */
    private Flags(Set flags) {
        flags = new HashSet(flags);
    }

    /**
     * Create new flags with the flags in <code>other</code> also set.
     */
    public Flags set(Flags other) {
        Flags f = new Flags();
        f.flags.addAll(flags);
        f.flags.addAll(other.flags);
        return f;
    }

    /**
     * Create new flags with the flags in <code>other</code> cleared.
     */
    public Flags clear(Flags other) {
        Flags f = new Flags();
        f.flags.addAll(flags);
        f.flags.removeAll(other.flags);
        return f;
    }

    /**
     * Create new flags with only flags in <code>other</code> set.
     */
    public Flags retain(Flags other) {
        Flags f = new Flags();
        f.flags.addAll(flags);
        f.flags.retainAll(other.flags);
        return f;
    }

    /**
     * Check if <i>any</i> flags in <code>other</code> are set.
     */
    public boolean intersects(Flags other) {
        for (Iterator i = other.flags.iterator(); i.hasNext(); ) {
            Flag f = (Flag) i.next();
            if (flags.contains(f))
                return true;
        }

        return false;
    }

    /**
     * Check if <i>all</i> flags in <code>other</code> are set.
     */
    public boolean contains(Flags other) {
        return flags.containsAll(other.flags);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>public</code>
     * flag set.
     */
    public Flags Public() {
	return set(PUBLIC);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>public</code>
     * flag clear.
     */
    public Flags clearPublic() {
	return clear(PUBLIC);
    }

    /**
     * Return true if <code>this</code> has the <code>public</code> flag set.
     */
    public boolean isPublic() {
	return contains(PUBLIC);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>private</code>
     * flag set.
     */
    public Flags Private() {
	return set(PRIVATE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>private</code>
     * flag clear.
     */
    public Flags clearPrivate() {
	return clear(PRIVATE);
    }

    /**
     * Return true if <code>this</code> has the <code>private</code> flag set.
     */
    public boolean isPrivate() {
	return contains(PRIVATE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>protected</code>
     * flag set.
     */
    public Flags Protected() {
	return set(PROTECTED);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>protected</code>
     * flag clear.
     */
    public Flags clearProtected() {
	return clear(PROTECTED);
    }

    /**
     * Return true if <code>this</code> has the <code>protected</code> flag set.
     */
    public boolean isProtected() {
	return contains(PROTECTED);
    }

    /**
     * Return a copy of this <code>this</code> with no access flags
     * (<code>public</code>, <code>private</code>, <code>protected</code>) set.
     */
    public Flags Package() {
        return clear(ACCESS_FLAGS);
    }

    /**
     * Return true if <code>this</code> has the no access flags
     * (<code>public</code>, <code>private</code>, <code>protected</code>) set.
     */
    public boolean isPackage() {
        return ! intersects(ACCESS_FLAGS);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>static</code>
     * flag set.
     */
    public Flags Static() {
	return set(STATIC);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>static</code>
     * flag clear.
     */
    public Flags clearStatic() {
	return clear(STATIC);
    }

    /**
     * Return true if <code>this</code> has the <code>static</code> flag set.
     */
    public boolean isStatic() {
	return contains(STATIC);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>final</code>
     * flag set.
     */
    public Flags Final() {
	return set(FINAL);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>final</code>
     * flag clear.
     */
    public Flags clearFinal() {
	return clear(FINAL);
    }

    /**
     * Return true if <code>this</code> has the <code>final</code> flag set.
     */
    public boolean isFinal() {
	return contains(FINAL);
    }

    /**
     * Return a copy of this <code>this</code> with the
     * <code>synchronized</code> flag set.
     */
    public Flags Synchronized() {
	return set(SYNCHRONIZED);
    }

    /**
     * Return a copy of this <code>this</code> with the
     * <code>synchronized</code> flag clear.
     */
    public Flags clearSynchronized() {
	return clear(SYNCHRONIZED);
    }

    /**
     * Return true if <code>this</code> has the <code>synchronized</code> flag
     * set.
     */
    public boolean isSynchronized() {
	return contains(SYNCHRONIZED);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>transient</code>
     * flag set.
     */
    public Flags Transient() {
	return set(TRANSIENT);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>transient</code>
     * flag clear.
     */
    public Flags clearTransient() {
	return clear(TRANSIENT);
    }

    /**
     * Return true if <code>this</code> has the <code>transient</code> flag set.
     */
    public boolean isTransient() {
	return contains(TRANSIENT);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>native</code>
     * flag set.
     */
    public Flags Native() {
	return set(NATIVE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>native</code>
     * flag clear.
     */
    public Flags clearNative() {
	return clear(NATIVE);
    }

    /**
     * Return true if <code>this</code> has the <code>native</code> flag set.
     */
    public boolean isNative() {
	return contains(NATIVE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>interface</code>
     * flag set.
     */
    public Flags Interface() {
	return set(INTERFACE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>interface</code>
     * flag clear.
     */
    public Flags clearInterface() {
	return clear(INTERFACE);
    }

    /**
     * Return true if <code>this</code> has the <code>interface</code> flag set.
     */
    public boolean isInterface() {
	return contains(INTERFACE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>abstract</code>
     * flag set.
     */
    public Flags Abstract() {
	return set(ABSTRACT);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>abstract</code>
     * flag clear.
     */
    public Flags clearAbstract() {
	return clear(ABSTRACT);
    }

    /**
     * Return true if <code>this</code> has the <code>abstract</code> flag set.
     */
    public boolean isAbstract() {
	return contains(ABSTRACT);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>volatile</code>
     * flag set.
     */
    public Flags Volatile() {
	return set(VOLATILE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>volatile</code>
     * flag clear.
     */
    public Flags clearVolatile() {
	return clear(VOLATILE);
    }

    /**
     * Return true if <code>this</code> has the <code>volatile</code> flag set.
     */
    public boolean isVolatile() {
	return contains(VOLATILE);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>strictfp</code>
     * flag set.
     */
    public Flags StrictFP() {
	return set(STRICTFP);
    }

    /**
     * Return a copy of this <code>this</code> with the <code>strictfp</code>
     * flag clear.
     */
    public Flags clearStrictFP() {
	return clear(STRICTFP);
    }

    /**
     * Return true if <code>this</code> has the <code>strictfp</code> flag set.
     */
    public boolean isStrictFP() {
	return contains(STRICTFP);
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

        List l = new LinkedList(all_flags);
        l.retainAll(flags);

        for (Iterator i = l.iterator(); i.hasNext(); ) {
            Flag f = (Flag) i.next();
            s += f.toString();
            if (i.hasNext())
                s += " ";
        }

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
        return flags.hashCode();
    }

    public boolean equals(Object o) {
	return o instanceof Flags && flags.equals(((Flags) o).flags);
    }
}

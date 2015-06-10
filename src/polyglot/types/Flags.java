/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.types;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import polyglot.util.Copy;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

/**
 * {@code Flags} is an immutable set of class, method, or field modifiers.
 * We represent package scope as the absence of private, public and protected
 * scope modifiers.
 */
public class Flags implements Serializable, Copy<Flags> {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Set<String> flags;
    protected Position position;

    protected static class FlagComparator implements Comparator<String>,
            Serializable {
        private static final long serialVersionUID =
                SerialVersionUID.generate();
        protected static final FlagComparator instance = new FlagComparator();
        protected static Map<String, Integer> ordering = new HashMap<>();
        protected static Map<Integer, String> revOrdering = new HashMap<>();

        @Override
        public int compare(String o1, String o2) {
            if (ordering.containsKey(o1) && ordering.containsKey(o2))
                return ordering.get(o1) - ordering.get(o2);
            return o1.compareTo(o2);
        }

        @SuppressWarnings("unused")
        private static final long readResolveVersionUID = 1L;

        private Object readResolve() {
            // If you update this method in an incompatible way, increment
            // readResolveVersionUID.
            return instance;
        }
    }

    public static final Flags NONE = new Flags();
    public static final Flags PUBLIC = createFlag("public", null);
    public static final Flags PROTECTED = createFlag("protected", null);
    public static final Flags PRIVATE = createFlag("private", null);
    public static final Flags ABSTRACT = createFlag("abstract", null);
    public static final Flags STATIC = createFlag("static", null);
    public static final Flags FINAL = createFlag("final", null);
    public static final Flags SYNCHRONIZED = createFlag("synchronized", null);
    public static final Flags NATIVE = createFlag("native", null);
    public static final Flags STRICTFP = createFlag("strictfp", null);
    public static final Flags TRANSIENT = createFlag("transient", null);
    public static final Flags VOLATILE = createFlag("volatile", null);
    public static final Flags INTERFACE = createFlag("interface", null);

    /** All access flags. */
    protected static final Flags ACCESS_FLAGS = PUBLIC.set(PRIVATE)
                                                      .set(PROTECTED);

    /**
     * Return a new Flags object with a new name.  Should be called only once
     * per name.
     *
     * @param name the name of the new flag
     * @param after the flags after which this flag should be printed;
     *        Flags.NONE to print before all other flags, null
     *        if we should print at the end.
     */
    public static Flags createFlag(String name, Flags after) {
        addToOrder(name, after);

        return new Flags(name);
    }

    public static void addToOrder(String name, Flags after) {
        Map<String, Integer> ordering = FlagComparator.ordering;
        Map<Integer, String> revOrdering = FlagComparator.revOrdering;
        if (ordering.containsKey(name))
            throw new InternalCompilerError("Flag " + name + " already added.");

        int index;
        if (after == null)
            index = ordering.size() + 1;
        else {
            index = 0;
            for (String s : after.flags) {
                int si = ordering.get(s);
                if (si > index) index = si;
            }
            // shift indices of existing flags
            for (int i = ordering.size(); i > index; i--) {
                String s = revOrdering.get(i);
                ordering.put(s, i + 1);
                revOrdering.put(i + 1, s);
            }
            index++;
        }
        ordering.put(name, index);
        revOrdering.put(index, name);
    }

    /**
     * Effects: returns a new access flags object with no access flags set.
     */
    protected Flags() {
        this.flags = new TreeSet<>(FlagComparator.instance);
        position = Position.compilerGenerated();
    }

    protected Flags(String name) {
        this();
        flags.add(name);
    }

    public Set<String> flags() {
        return this.flags;
    }

    public Position position() {
        return position;
    }

    public Flags position(Position position) {
        return position(this, position);
    }

    protected Flags position(Flags flags, Position position) {
        if (flags.position == position) return flags;
        flags = copyIfNeeded(flags);
        flags.position = position;
        return flags;
    }

    protected Flags copyIfNeeded(Flags flags) {
        if (flags == this) flags = Copy.Util.copy(flags);
        return flags;
    }

    @Override
    public Flags copy() {
        try {
            Flags flags = (Flags) super.clone();
            return flags;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }

    /**
     * Create new flags with the flags in {@code other} also set.
     */
    public Flags set(Flags other) {
        Flags f = new Flags();
        f.flags.addAll(this.flags);
        f.flags.addAll(other.flags);

        Position start = Position.first(position, other.position);
        Position end = Position.last(position, other.position);

        if (start == null || end == null) {
            f.position = Position.compilerGenerated();
        }
        else {
            f.position = new Position(start, end);
        }

        return f;
    }

    /**
     * Create new flags with the flags in {@code other} cleared.
     */
    public Flags clear(Flags other) {
        Flags f = new Flags();
        f.flags.addAll(this.flags);
        f.flags.removeAll(other.flags);
        f.position = Position.compilerGenerated();
        return f;
    }

    /**
     * Create new flags with only flags in {@code other} set.
     */
    public Flags retain(Flags other) {
        Flags f = new Flags();
        f.flags.addAll(this.flags);
        f.flags.retainAll(other.flags);
        f.position = Position.compilerGenerated();
        return f;
    }

    /**
     * Check if <i>any</i> flags in {@code other} are set.
     */
    public boolean intersects(Flags other) {
        for (String name : this.flags) {
            if (other.flags.contains(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if <i>all</i> flags in {@code other} are set.
     */
    public boolean contains(Flags other) {
        return this.flags.containsAll(other.flags);
    }

    /**
     * Return a copy of this {@code this} with the {@code public}
     * flag set.
     */
    public Flags Public() {
        return set(PUBLIC);
    }

    /**
     * Return a copy of this {@code this} with the {@code public}
     * flag clear.
     */
    public Flags clearPublic() {
        return clear(PUBLIC);
    }

    /**
     * Return true if {@code this} has the {@code public} flag set.
     */
    public boolean isPublic() {
        return contains(PUBLIC);
    }

    /**
     * Return a copy of this {@code this} with the {@code private}
     * flag set.
     */
    public Flags Private() {
        return set(PRIVATE);
    }

    /**
     * Return a copy of this {@code this} with the {@code private}
     * flag clear.
     */
    public Flags clearPrivate() {
        return clear(PRIVATE);
    }

    /**
     * Return true if {@code this} has the {@code private} flag set.
     */
    public boolean isPrivate() {
        return contains(PRIVATE);
    }

    /**
     * Return a copy of this {@code this} with the {@code protected}
     * flag set.
     */
    public Flags Protected() {
        return set(PROTECTED);
    }

    /**
     * Return a copy of this {@code this} with the {@code protected}
     * flag clear.
     */
    public Flags clearProtected() {
        return clear(PROTECTED);
    }

    /**
     * Return true if {@code this} has the {@code protected} flag set.
     */
    public boolean isProtected() {
        return contains(PROTECTED);
    }

    /**
     * Return a copy of this {@code this} with no access flags
     * ({@code public}, {@code private}, {@code protected}) set.
     */
    public Flags Package() {
        return clear(ACCESS_FLAGS);
    }

    /**
     * Return true if {@code this} has the no access flags
     * ({@code public}, {@code private}, {@code protected}) set.
     */
    public boolean isPackage() {
        return !intersects(ACCESS_FLAGS);
    }

    /**
     * Return a copy of this {@code this} with the {@code static}
     * flag set.
     */
    public Flags Static() {
        return set(STATIC);
    }

    /**
     * Return a copy of this {@code this} with the {@code static}
     * flag clear.
     */
    public Flags clearStatic() {
        return clear(STATIC);
    }

    /**
     * Return true if {@code this} has the {@code static} flag set.
     */
    public boolean isStatic() {
        return contains(STATIC);
    }

    /**
     * Return a copy of this {@code this} with the {@code final}
     * flag set.
     */
    public Flags Final() {
        return set(FINAL);
    }

    /**
     * Return a copy of this {@code this} with the {@code final}
     * flag clear.
     */
    public Flags clearFinal() {
        return clear(FINAL);
    }

    /**
     * Return true if {@code this} has the {@code final} flag set.
     */
    public boolean isFinal() {
        return contains(FINAL);
    }

    /**
     * Return a copy of this {@code this} with the
     * {@code synchronized} flag set.
     */
    public Flags Synchronized() {
        return set(SYNCHRONIZED);
    }

    /**
     * Return a copy of this {@code this} with the
     * {@code synchronized} flag clear.
     */
    public Flags clearSynchronized() {
        return clear(SYNCHRONIZED);
    }

    /**
     * Return true if {@code this} has the {@code synchronized} flag
     * set.
     */
    public boolean isSynchronized() {
        return contains(SYNCHRONIZED);
    }

    /**
     * Return a copy of this {@code this} with the {@code transient}
     * flag set.
     */
    public Flags Transient() {
        return set(TRANSIENT);
    }

    /**
     * Return a copy of this {@code this} with the {@code transient}
     * flag clear.
     */
    public Flags clearTransient() {
        return clear(TRANSIENT);
    }

    /**
     * Return true if {@code this} has the {@code transient} flag set.
     */
    public boolean isTransient() {
        return contains(TRANSIENT);
    }

    /**
     * Return a copy of this {@code this} with the {@code native}
     * flag set.
     */
    public Flags Native() {
        return set(NATIVE);
    }

    /**
     * Return a copy of this {@code this} with the {@code native}
     * flag clear.
     */
    public Flags clearNative() {
        return clear(NATIVE);
    }

    /**
     * Return true if {@code this} has the {@code native} flag set.
     */
    public boolean isNative() {
        return contains(NATIVE);
    }

    /**
     * Return a copy of this {@code this} with the {@code interface}
     * flag set.
     */
    public Flags Interface() {
        return set(INTERFACE);
    }

    /**
     * Return a copy of this {@code this} with the {@code interface}
     * flag clear.
     */
    public Flags clearInterface() {
        return clear(INTERFACE);
    }

    /**
     * Return true if {@code this} has the {@code interface} flag set.
     */
    public boolean isInterface() {
        return contains(INTERFACE);
    }

    /**
     * Return a copy of this {@code this} with the {@code abstract}
     * flag set.
     */
    public Flags Abstract() {
        return set(ABSTRACT);
    }

    /**
     * Return a copy of this {@code this} with the {@code abstract}
     * flag clear.
     */
    public Flags clearAbstract() {
        return clear(ABSTRACT);
    }

    /**
     * Return true if {@code this} has the {@code abstract} flag set.
     */
    public boolean isAbstract() {
        return contains(ABSTRACT);
    }

    /**
     * Return a copy of this {@code this} with the {@code volatile}
     * flag set.
     */
    public Flags Volatile() {
        return set(VOLATILE);
    }

    /**
     * Return a copy of this {@code this} with the {@code volatile}
     * flag clear.
     */
    public Flags clearVolatile() {
        return clear(VOLATILE);
    }

    /**
     * Return true if {@code this} has the {@code volatile} flag set.
     */
    public boolean isVolatile() {
        return contains(VOLATILE);
    }

    /**
     * Return a copy of this {@code this} with the {@code strictfp}
     * flag set.
     */
    public Flags StrictFP() {
        return set(STRICTFP);
    }

    /**
     * Return a copy of this {@code this} with the {@code strictfp}
     * flag clear.
     */
    public Flags clearStrictFP() {
        return clear(STRICTFP);
    }

    /**
     * Return true if {@code this} has the {@code strictfp} flag set.
     */
    public boolean isStrictFP() {
        return contains(STRICTFP);
    }

    /**
     * Return true if {@code this} has more restrictive access flags than
     * {@code f}.
     */
    public boolean moreRestrictiveThan(Flags f) {
        if (isPrivate() && (f.isProtected() || f.isPackage() || f.isPublic())) {
            return true;
        }

        if (isPackage() && (f.isProtected() || f.isPublic())) {
            return true;
        }

        if (isProtected() && f.isPublic()) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return translate().trim();
    }

    /**
     * Return "" if no flags set, or toString() + " " if some flags are set.
     */
    public String translate() {
        StringBuffer sb = new StringBuffer();

        for (String s : this.flags) {
            sb.append(s);
            sb.append(" ");
        }

        return sb.toString();
    }

    @Override
    public int hashCode() {
        return flags.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Flags && flags.equals(((Flags) o).flags);
    }

    public boolean isEmpty() {
        return flags.isEmpty();
    }
}

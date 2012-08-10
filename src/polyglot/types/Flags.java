/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * <code>Flags</code> is an immutable set of class, method, or field modifiers.
 * We represent package scope as the absence of private, public and protected
 * scope modifiers.
 */
public class Flags implements Serializable {
    protected Set<String> flags;

    protected static class FlagComparator implements Comparator<String> {
        protected static List<String> order =
                new ArrayList<String>(Arrays.asList("public",
                                                    "private",
                                                    "protected",
                                                    "static",
                                                    "final",
                                                    "synchronized",
                                                    "transient",
                                                    "native",
                                                    "interface",
                                                    "abstract",
                                                    "volatile",
                                                    "strictfp"));

        @Override
        public int compare(String o1, String o2) {
            if (o1.equals(o2)) return 0;

            for (int i = 0; i < order.size(); i++) {
                if (o1.equals(order.get(i))) return -1;
                if (o2.equals(order.get(i))) return 1;
            }

            return o1.compareTo(o2);
        }
    }

    public static final Flags NONE = new Flags();
    public static final Flags PUBLIC = createFlag("public", null);
    public static final Flags PRIVATE = createFlag("private", null);
    public static final Flags PROTECTED = createFlag("protected", null);
    public static final Flags STATIC = createFlag("static", null);
    public static final Flags FINAL = createFlag("final", null);
    public static final Flags SYNCHRONIZED = createFlag("synchronized", null);
    public static final Flags TRANSIENT = createFlag("transient", null);
    public static final Flags NATIVE = createFlag("native", null);
    public static final Flags INTERFACE = createFlag("interface", null);
    public static final Flags ABSTRACT = createFlag("abstract", null);
    public static final Flags VOLATILE = createFlag("volatile", null);
    public static final Flags STRICTFP = createFlag("strictfp", null);

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
        List<String> order = FlagComparator.order;
        boolean added = false;

        if (after == null) {
            order.add(name);
        }
        else if (after.flags.isEmpty()) {
            order.add(0, name);
        }
        else {
            for (ListIterator<String> i = order.listIterator(); i.hasNext();) {
                String s = i.next();
                after = after.clear(new Flags(s));
                if (after.flags.isEmpty()) {
                    i.add(name);
                    added = true;
                    break;
                }
            }

            if (!added) {
                // shouldn't happen
                order.add(name);
            }
        }
    }

    /**
     * Effects: returns a new accessflags object with no accessflags set.
     */
    protected Flags() {
        this.flags = new TreeSet<String>();
    }

    protected Flags(String name) {
        this();
        flags.add(name);
    }

    public Set<String> flags() {
        return this.flags;
    }

    /**
     * Create new flags with the flags in <code>other</code> also set.
     */
    public Flags set(Flags other) {
        Flags f = new Flags();
        f.flags.addAll(this.flags);
        f.flags.addAll(other.flags);
        return f;
    }

    /**
     * Create new flags with the flags in <code>other</code> cleared.
     */
    public Flags clear(Flags other) {
        Flags f = new Flags();
        f.flags.addAll(this.flags);
        f.flags.removeAll(other.flags);
        return f;
    }

    /**
     * Create new flags with only flags in <code>other</code> set.
     */
    public Flags retain(Flags other) {
        Flags f = new Flags();
        f.flags.addAll(this.flags);
        f.flags.retainAll(other.flags);
        return f;
    }

    /**
     * Check if <i>any</i> flags in <code>other</code> are set.
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
     * Check if <i>all</i> flags in <code>other</code> are set.
     */
    public boolean contains(Flags other) {
        return this.flags.containsAll(other.flags);
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
        return !intersects(ACCESS_FLAGS);
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
}

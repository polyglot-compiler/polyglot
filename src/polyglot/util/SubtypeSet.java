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

package polyglot.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import polyglot.types.Type;
import polyglot.types.TypeSystem;

/**
 * Class to implement sets containing <code>polyglot.types.Type </code>.  
 * Set membership is based on the subtype relationships.  Thus, if 
 * <code>S</code> is a supertype of <code>A</code> and <code>B</code>, then
 * { <code>S</code> } union { <code>A</code>,<code>B</code> } = 
 * { <code>S</code> }.  Similarily, we remove elements from the set such 
 * that if <code>s</code> is an element of a set <code>S</code>, then a
 * call to remove <code>r</code> removes all <code>s</code> s.t. r is a 
 * a supertype of s.
 */
public class SubtypeSet implements java.util.Set<Type> {
    protected List<Type> v;
    protected TypeSystem ts;
    protected Type topType; // Everything in the set must be a subtype of topType.

    /**
     * Creates an empty SubtypeSet
     */
    public SubtypeSet(TypeSystem ts) {
        this(ts.Object());
    }

    public SubtypeSet(Type top) {
        v = new ArrayList<Type>();
        this.ts = top.typeSystem();
        this.topType = top;
    }

    /**
     * Creates a copy of the given SubtypeSet
     */
    public SubtypeSet(SubtypeSet s) {
        v = new ArrayList<Type>(s.v);
        ts = s.ts;
        topType = s.topType;
    }

    public SubtypeSet(TypeSystem ts, Collection<? extends Type> c) {
        this(ts);
        addAll(c);
    }

    public SubtypeSet(Type top, Collection<? extends Type> c) {
        this(top);
        addAll(c);
    }

    /**
     * Add an element of type <code>polyglot.types.Type</code> to the set
     * only if it has no supertypes already in the set. If we do add it, 
     * remove any subtypes of <code>o</code>
     * 
     * @param type The element to add.
     */
    @Override
    public boolean add(Type type) {
        if (type == null) {
            return false;
        }

        if (ts.isSubtype(type, topType)) {
            boolean haveToAdd = true;

            for (Iterator<Type> i = v.iterator(); i.hasNext();) {
                Type t = i.next();

                if (ts.descendsFrom(t, type)) {
                    i.remove();
                }

                if (ts.isSubtype(type, t)) {
                    haveToAdd = false;
                    break;
                }
            }

            if (haveToAdd) {
                v.add(type);
            }

            return haveToAdd;
        }

        throw new InternalCompilerError("Can only add " + topType
                + "s to the set. Got a " + type);
    }

    /**
     * Adds all elements from c into this set.
     */
    @Override
    public boolean addAll(Collection<? extends Type> c) {
        if (c == null) {
            return false;
        }

        boolean changed = false;

        for (Type t : c) {
            changed |= add(t);
        }

        return changed;
    }

    /**
     * Removes all elements from the set
     */
    @Override
    public void clear() {
        v.clear();
    }

    /**
     * Check whether object <code>o</code> is in the set. Because of the 
     * semantics of the subtype set, <code>o</code> is in the set iff
     * it descends from (or is equal to) one of the elements in the set.
     */
    @Override
    public boolean contains(Object o) {
        if (o instanceof Type) {
            Type type = (Type) o;

            for (Type t : v) {
                if (ts.isSubtype(type, t)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check whether the type <code>t</code> or a subtype is in the set.
     * Returns true iff it descends from, is equal to, or is a super type of
     * one of the elements in the set.
     */
    public boolean containsSubtype(Type type) {
        for (Type t : v) {
            if (ts.isSubtype(type, t) || ts.isSubtype(t, type)) return true;
        }

        return false;
    }

    /**
     * Checks whether all elements of the collection are in the set
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isEmpty() {
        return v.isEmpty();
    }

    @Override
    public Iterator<Type> iterator() {
        return v.iterator();
    }

    /**
     * Removes all elements <code>s</code> in the set such that 
     * <code>s</code> decends from <code>o</code>
     *
     * @return whether or not an element was removed.
     */
    @Override
    public boolean remove(Object o) {
        Type type = (Type) o;

        boolean removed = false;

        for (Iterator<Type> i = v.iterator(); i.hasNext();) {
            Type t = i.next();

            if (ts.isSubtype(t, type)) {
                removed = true;
                i.remove();
            }
        }

        return removed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;

        for (Object o : c) {
            changed |= remove(o);
        }

        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int size() {
        return v.size();
    }

    @Override
    public Object[] toArray() {
        return v.toArray();
    }

    @Override
    public <U> U[] toArray(U[] a) {
        return v.toArray(a);
    }

    @Override
    public String toString() {
        return v.toString();
    }
}

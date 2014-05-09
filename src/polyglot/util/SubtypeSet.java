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

package polyglot.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import polyglot.types.Type;
import polyglot.types.TypeSystem;

/**
 * Class to implement sets containing {@code polyglot.types.Type}.  
 * Set membership is based on the subtype relationships.  Thus, if 
 * {@code S} is a supertype of {@code A} and {@code B}, then
 * { {@code S} } union { {@code A}, {@code B} } = 
 * { {@code S} }.  Similarly, we remove elements from the set such 
 * that if {@code s} is an element of a set {@code S}, then a
 * call to remove {@code r} removes all {@code s} s.t. r is a 
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
        v = new ArrayList<>();
        this.ts = top.typeSystem();
        this.topType = top;
    }

    /**
     * Creates a copy of the given SubtypeSet
     */
    public SubtypeSet(SubtypeSet s) {
        v = new ArrayList<>(s.v);
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
     * Add an element of type {@code polyglot.types.Type} to the set
     * only if it has no supertypes already in the set. If we do add it, 
     * remove any subtypes of {@code o}
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
     * Check whether object {@code o} is in the set. Because of the 
     * semantics of the subtype set, {@code o} is in the set iff
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
     * Check whether the type {@code t} or a subtype is in the set.
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
     * Removes all elements {@code s} in the set such that 
     * {@code s} decends from {@code o}
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
        boolean changed = false;
        for (ListIterator<Type> itr = v.listIterator(); itr.hasNext();) {
            Type t = itr.next();
            Type glb = null;
            for (Object o : c) {
                if (o instanceof Type) {
                    Type type = (Type) o;
                    if (glb == null) {
                        if (ts.isSubtype(type, t))
                            glb = type;
                        else if (ts.isSubtype(t, type)) glb = t;
                    }
                    else if (ts.isSubtype(type, glb)) glb = type;
                }
            }
            if (glb != t) {
                changed = true;
                if (glb == null)
                    itr.remove();
                else itr.set(glb);;
            }
        }
        return changed;
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

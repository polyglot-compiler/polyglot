/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import java.util.HashSet;
import java.util.Iterator;

import polyglot.types.TypeObject;
import polyglot.types.TypeObject_c;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class KeySet_c extends TypeObject_c implements KeySet {
    private static final long serialVersionUID = SerialVersionUID.generate();

    HashSet<Key> set;

    public KeySet_c(TypeSystem ts, Position pos) {
        super(ts, pos);
        this.set = new HashSet<Key>();
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public Iterator<Key> iterator() {
        return set.iterator();
    }

    @Override
    public boolean contains(Key key) {
        return set.contains(key);
    }

    @Override
    public KeySet add(Key key) {
        if (set.contains(key)) return this;
        KeySet_c s = (KeySet_c) copy();
        s.set = new HashSet<Key>(set);
        s.set.add(key);
        return s;
    }

    @Override
    public KeySet remove(Key key) {
        if (!set.contains(key)) return this;
        KeySet_c s = (KeySet_c) copy();
        s.set = new HashSet<Key>(set);
        s.set.remove(key);
        return s;
    }

    @Override
    public KeySet addAll(KeySet keys) {
        KeySet_c other = (KeySet_c) keys;
        KeySet_c s = (KeySet_c) copy();
        s.set = new HashSet<Key>(set);
        s.set.addAll(other.set);
        return s;
    }

    @Override
    public KeySet removeAll(KeySet keys) {
        KeySet_c other = (KeySet_c) keys;
        KeySet_c s = (KeySet_c) copy();
        s.set = new HashSet<Key>(set);
        s.set.removeAll(other.set);
        return s;
    }

    @Override
    public KeySet retainAll(KeySet keys) {
        KeySet_c other = (KeySet_c) keys;
        KeySet_c s = (KeySet_c) copy();
        s.set = new HashSet<Key>(set);
        s.set.retainAll(other.set);
        return s;
    }

    @Override
    public boolean containsAll(KeySet keys) {
        KeySet_c other = (KeySet_c) keys;
        return set.containsAll(other.set);
    }

    @Override
    public boolean equalsImpl(TypeObject o) {
        if (o instanceof KeySet_c) {
            KeySet_c other = (KeySet_c) o;
            return set.equals(other.set);
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean isCanonical() {
        for (Key k : set) {
            if (!k.isCanonical()) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return set.toString().replace('[', '{').replace(']', '}');
    }
}

/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.types.TypeObject;

public interface KeySet extends TypeObject, Iterable<Key> {
    int size();

    boolean contains(Key key);

    KeySet add(Key key);

    KeySet remove(Key key);

    boolean containsAll(KeySet keys);

    KeySet addAll(KeySet keys);

    KeySet removeAll(KeySet keys);

    KeySet retainAll(KeySet keys);

    boolean isEmpty();
}

/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

import java.util.*;

public interface KeySet extends TypeObject
{
    int size();
    Iterator iterator();
    boolean contains(Key key);
    KeySet add(Key key);
    KeySet remove(Key key);
    boolean containsAll(KeySet keys);
    KeySet addAll(KeySet keys);
    KeySet removeAll(KeySet keys);
    KeySet retainAll(KeySet keys);
    boolean isEmpty();
}

/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import java.util.List;

import polyglot.ext.param.types.Subst;

public interface CofferSubst extends Subst<Key, Key> {
    public Key substKey(Key key);

    public KeySet substKeySet(KeySet key);

    public ThrowConstraint substThrowConstraint(ThrowConstraint c);

    public List<ThrowConstraint> substThrowConstraintList(
            List<ThrowConstraint> l);

    public Key get(Key formal);

    public void put(Key formal, Key actual);
}

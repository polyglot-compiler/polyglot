/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import java.util.List;
import java.util.Map;

import polyglot.ext.param.types.Subst_c;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.MethodInstance;
import polyglot.types.Type;
import polyglot.util.CachingTransformingList;
import polyglot.util.Transformation;

public class CofferSubst_c extends Subst_c<Key, Key> implements CofferSubst {
    public CofferSubst_c(CofferTypeSystem ts, Map<Key, ? extends Key> subst) {
        super(ts, subst);
    }

    @Override
    public Key get(Key formal) {
        return subst.get(formal);
    }

    @Override
    public void put(Key formal, Key actual) {
        subst.put(formal, actual);
    }

    ////////////////////////////////////////////////////////////////
    // Override substitution methods to handle Coffer constructs

    @Override
    public ClassType substClassType(ClassType t) {
        // Don't bother trying to substitute into a non-Coffer class.
        if (!(t instanceof CofferClassType)) {
            return t;
        }

        return new CofferSubstClassType_c((CofferTypeSystem) ts,
                                          t.position(),
                                          (CofferClassType) t,
                                          this);
    }

    @Override
    public <T extends MethodInstance> T substMethod(T mi) {
        mi = super.substMethod(mi);

        if (mi instanceof CofferMethodInstance) {
            CofferMethodInstance vmi = (CofferMethodInstance) mi.copy();

            vmi.setEntryKeys(substKeySet(vmi.entryKeys()));
            vmi.setReturnKeys(substKeySet(vmi.returnKeys()));
            vmi.setThrowConstraints(substThrowConstraintList(vmi.throwConstraints()));

            @SuppressWarnings("unchecked")
            T result = (T) vmi;
            return result;
        }

        return mi;
    }

    @Override
    public <T extends ConstructorInstance> T substConstructor(T ci) {
        ci = super.substConstructor(ci);

        if (ci instanceof CofferConstructorInstance) {
            CofferConstructorInstance vci =
                    (CofferConstructorInstance) ci.copy();

            vci.setEntryKeys(substKeySet(vci.entryKeys()));
            vci.setReturnKeys(substKeySet(vci.returnKeys()));
            vci.setThrowConstraints(substThrowConstraintList(vci.throwConstraints()));

            @SuppressWarnings("unchecked")
            T result = (T) vci;
            return result;
        }

        return ci;
    }

    ////////////////////////////////////////////////////////////////
    // Substitution methods for Coffer constructs

    public class ConstraintXform implements
            Transformation<ThrowConstraint, ThrowConstraint> {
        @Override
        public ThrowConstraint transform(ThrowConstraint tc) {
            return substThrowConstraint(tc);
        }
    }

    @Override
    public List<ThrowConstraint> substThrowConstraintList(
            List<ThrowConstraint> l) {
        return new CachingTransformingList<ThrowConstraint, ThrowConstraint>(l,
                                                                             new ConstraintXform());
    }

    @Override
    public ThrowConstraint substThrowConstraint(ThrowConstraint c) {
        if (c == null) {
            return null;
        }

        Type t = substType(c.throwType());
        KeySet k = substKeySet(c.keys());

        if (t != c.throwType() || k != c.keys()) {
            c = (ThrowConstraint) c.copy();
            c.setThrowType(t);
            c.setKeys(k);
        }

        return c;
    }

    @Override
    public KeySet substKeySet(KeySet keys) {
        if (keys == null) {
            return null;
        }

        boolean changed = false;

        CofferTypeSystem ts = (CofferTypeSystem) this.ts;
        KeySet newKeys = ts.emptyKeySet(keys.position());
        for (Key key : keys) {
            Key key2 = substKey(key);
            if (key != key2) changed = true;
            newKeys = newKeys.add(substKey(key));
        }

        if (!changed) newKeys = keys;

        return newKeys;
    }

    @Override
    public Key substKey(Key key) {
        if (key == null) {
            return null;
        }

        Key newKey = subst.get(key);

        if (newKey != null) {
            return newKey;
        }

        return key;
    }
}

/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.types.*;
import polyglot.ext.param.types.*;
import polyglot.util.*;
import java.util.*;

public class CofferSubst_c extends Subst_c implements CofferSubst
{
    public CofferSubst_c(CofferTypeSystem ts, Map subst, Map cache) {
        super(ts, subst, cache);

        for (Iterator i = entries(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            if (e.getKey() instanceof Key && e.getValue() instanceof Key)
                continue;
            throw new InternalCompilerError("bad map: " + subst);
        }
    }

    public Key get(Key formal) {
        return (Key) subst.get(formal);
    }

    public void put(Key formal, Key actual) {
        subst.put(formal, actual);
    }

    ////////////////////////////////////////////////////////////////
    // Override substitution methods to handle Coffer constructs

    public ClassType substClassType(ClassType t) {
        // Don't bother trying to substitute into a non-Coffer class.
        if (! (t instanceof CofferClassType)) {
            return t;
        }

        return new CofferSubstClassType_c((CofferTypeSystem) ts, t.position(),
                                         (CofferClassType) t, this);
    }

    public MethodInstance substMethod(MethodInstance mi) {
        mi = super.substMethod(mi);

        if (mi instanceof CofferMethodInstance) {
            CofferMethodInstance vmi = (CofferMethodInstance) mi.copy();

            vmi.setEntryKeys(substKeySet(vmi.entryKeys()));
            vmi.setReturnKeys(substKeySet(vmi.returnKeys()));
            vmi.setThrowConstraints(substThrowConstraintList(vmi.throwConstraints()));

            mi = vmi;
        }

        return mi;
    }

    public ConstructorInstance substConstructor(ConstructorInstance ci) {
        ci = super.substConstructor(ci);

        if (ci instanceof CofferConstructorInstance) {
            CofferConstructorInstance vci = (CofferConstructorInstance) ci.copy();

            vci.setEntryKeys(substKeySet(vci.entryKeys()));
            vci.setReturnKeys(substKeySet(vci.returnKeys()));
            vci.setThrowConstraints(substThrowConstraintList(vci.throwConstraints()));

            ci = vci;
        }

        return ci;
    }

    ////////////////////////////////////////////////////////////////
    // Substitution methods for Coffer constructs

    public class ConstraintXform implements Transformation {
        public Object transform(Object o) {
            return substThrowConstraint((ThrowConstraint) o);
        }
    }

    public List substThrowConstraintList(List l) {
        return new CachingTransformingList(l, new ConstraintXform());
    }

    public ThrowConstraint substThrowConstraint(ThrowConstraint c) {
        if (c == null) {
            return null;
        }

        CofferTypeSystem ts = (CofferTypeSystem) this.ts;
        ThrowConstraint c2;

        Type t = substType(c.throwType());
        KeySet k = substKeySet(c.keys());

        if (t != c.throwType() || k != c.keys()) {
            c = (ThrowConstraint) c.copy();
            c.setThrowType(t);
            c.setKeys(k);
        }

        return c;
    }

    public KeySet substKeySet(KeySet keys) {
        if (keys == null) {
            return null;
        }

        boolean changed = false;

        CofferTypeSystem ts = (CofferTypeSystem) this.ts;
        KeySet newKeys = ts.emptyKeySet(keys.position());
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            Key key = (Key) i.next();
            Key key2 = substKey(key);
            if (key != key2)
                changed = true;
            newKeys = newKeys.add(substKey(key));
        }

        if (! changed)
            newKeys = keys;

        return newKeys;
    }

    public Key substKey(Key key) {
	if (key == null) {
	    return null;
	}

        Key newKey = (Key) subst.get(key);

        if (newKey != null) {
            return newKey;
        }

        return key;
    }
}

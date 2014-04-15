/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import java.util.ArrayList;
import java.util.List;

import polyglot.ext.param.types.PClass;
import polyglot.ext.param.types.SubstClassType_c;
import polyglot.types.ClassType;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class CofferSubstClassType_c extends SubstClassType_c<Key, Key>
        implements CofferSubstType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public CofferSubstClassType_c(CofferTypeSystem ts, Position pos,
            ClassType base, CofferSubst subst) {
        super(ts, pos, base, subst);
    }

    ////////////////////////////////////////////////////////////////
    // Implement methods of CofferSubstType

    @Override
    public PClass<Key, Key> instantiatedFrom() {
        return ((CofferParsedClassType) base).instantiatedFrom();
    }

    @Override
    public List<Key> actuals() {
        PClass<Key, Key> pc = instantiatedFrom();
        CofferSubst subst = (CofferSubst) this.subst;

        List<Key> actuals = new ArrayList<Key>(pc.formals().size());

        for (Key key : pc.formals()) {
            actuals.add(subst.substKey(key));
        }

        return actuals;
    }

    ////////////////////////////////////////////////////////////////
    // Implement methods of CofferClassType

    @Override
    public Key key() {
        CofferClassType base = (CofferClassType) this.base;
        CofferSubst subst = (CofferSubst) this.subst;
        return subst.substKey(base.key());
    }

    @Override
    public String toString() {
        return "tracked(" + subst + ") " + base;
    }
}

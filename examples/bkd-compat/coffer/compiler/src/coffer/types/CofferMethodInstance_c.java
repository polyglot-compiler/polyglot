/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.MethodInstance_c;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CachingTransformingList;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.Transformation;

/** An implementation of the <code>CofferMethodInstance</code> interface. 
 */
public class CofferMethodInstance_c extends MethodInstance_c implements
        CofferMethodInstance {
    protected KeySet entryKeys;
    protected KeySet returnKeys;
    protected List<ThrowConstraint> throwConstraints;

    public CofferMethodInstance_c(CofferTypeSystem ts, Position pos,
            ReferenceType container, Flags flags, Type returnType, String name,
            List<? extends Type> argTypes, KeySet entryKeys, KeySet returnKeys,
            List<ThrowConstraint> throwConstraints) {
        super(ts,
              pos,
              container,
              flags,
              returnType,
              name,
              argTypes,
              Collections.<Type> emptyList());

        this.entryKeys = entryKeys;
        this.returnKeys = returnKeys;
        this.throwConstraints =
                new ArrayList<ThrowConstraint>(throwConstraints);

        if (entryKeys == null)
            throw new InternalCompilerError("null entry keys for " + this);
    }

    @Override
    public boolean isCanonical() {
        for (ThrowConstraint c : throwConstraints) {
            if (!c.isCanonical()) {
                return false;
            }
        }

        if (!entryKeys.isCanonical()) {
            return false;
        }

        if (returnKeys != null && !returnKeys.isCanonical()) {
            return false;
        }

        return super.isCanonical();
    }

    @Override
    public KeySet entryKeys() {
        return entryKeys;
    }

    @Override
    public KeySet returnKeys() {
        return returnKeys;
    }

    @Override
    public List<ThrowConstraint> throwConstraints() {
        return throwConstraints;
    }

    @Override
    public List<Type> throwTypes() {
        return new CachingTransformingList<ThrowConstraint, Type>(throwConstraints,
                                                                  new GetType());
    }

    public class GetType implements Transformation<ThrowConstraint, Type> {
        @Override
        public Type transform(ThrowConstraint tc) {
            return tc.throwType();
        }
    }

    @Override
    public void setThrowTypes(List<? extends Type> throwTypes) {
        Iterator<? extends Type> i = throwTypes.iterator();
        Iterator<ThrowConstraint> j = throwConstraints.iterator();

        List<ThrowConstraint> l = new LinkedList<ThrowConstraint>();

        while (i.hasNext() && j.hasNext()) {
            Type t = i.next();
            ThrowConstraint c = j.next();
            if (t != c.throwType()) {
                c = (ThrowConstraint) c.copy();
                c.setThrowType(t);
            }

            l.add(c);
        }

        if (i.hasNext() || j.hasNext()) {
            throw new InternalCompilerError("unimplemented");
        }

        this.throwConstraints = l;
    }

    @Override
    public void setEntryKeys(KeySet entryKeys) {
        this.entryKeys = entryKeys;
    }

    @Override
    public void setReturnKeys(KeySet returnKeys) {
        this.returnKeys = returnKeys;
    }

    @Override
    public void setThrowConstraints(List<ThrowConstraint> throwConstraints) {
        this.throwConstraints =
                new ArrayList<ThrowConstraint>(throwConstraints);
    }

    @Override
    public boolean canOverrideImpl(MethodInstance mj, boolean quiet)
            throws SemanticException {
        CofferMethodInstance mi = this;

        KeySet e;
        KeySet r;
        List<ThrowConstraint> l;

        if (mj instanceof CofferMethodInstance) {
            e = ((CofferMethodInstance) mj).entryKeys();
            r = ((CofferMethodInstance) mj).returnKeys();
            l = ((CofferMethodInstance) mj).throwConstraints();
        }
        else {
            CofferTypeSystem ts = (CofferTypeSystem) this.ts;
            e = ts.emptyKeySet(position());
            r = ts.emptyKeySet(position());
            l = Collections.emptyList();
        }

        // Can pass through more keys.
        KeySet newKeys = entryKeys.removeAll(e);

        if (!returnKeys.equals(r.addAll(newKeys))) {
            if (quiet) return false;
            throw new SemanticException(mi.signature() + " in "
                    + mi.container() + " cannot override " + mj.signature()
                    + " in " + mj.container() + ";", mi.position());
        }

        CONSTRAINTS: for (ThrowConstraint c : throwConstraints) {
            for (ThrowConstraint superC : l) {
                if (superC.throwType().equals(c.throwType())) {
                    if (!c.keys().equals(superC.keys().addAll(newKeys))) {
                        if (quiet) return false;
                        throw new SemanticException(mi.signature() + " in "
                                + mi.container() + " cannot override "
                                + mj.signature() + " in " + mj.container()
                                + ";", mi.position());
                    }
                    continue CONSTRAINTS;
                }
            }

            if (!c.keys().equals(newKeys)) {
                if (quiet) return false;
                throw new SemanticException(mi.signature() + " in "
                        + mi.container() + " cannot override " + mj.signature()
                        + " in " + mj.container() + ";", mi.position());
            }
        }

        return super.canOverrideImpl(mj, quiet);
    }

    @Override
    public String toString() {
        return super.toString() + " " + entryKeys + "->" + returnKeys
                + " throws " + throwConstraints;
    }
}

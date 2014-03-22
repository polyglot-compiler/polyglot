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

import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance_c;
import polyglot.types.Flags;
import polyglot.types.Type;
import polyglot.util.CachingTransformingList;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.util.Transformation;

/** An implementation of the <code>CofferConstructorInstance</code> interface. 
 */
public class CofferConstructorInstance_c extends ConstructorInstance_c
        implements CofferConstructorInstance {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected KeySet entryKeys;
    protected KeySet returnKeys;
    protected List<ThrowConstraint> throwConstraints;

    public CofferConstructorInstance_c(CofferTypeSystem ts, Position pos,
            ClassType container, Flags flags, List<? extends Type> argTypes,
            KeySet entryKeys, KeySet returnKeys,
            List<ThrowConstraint> throwConstraints) {
        super(ts,
              pos,
              container,
              flags,
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
    public String toString() {
        return super.toString() + " " + entryKeys + "->" + returnKeys
                + " throws " + throwConstraints;
    }
}

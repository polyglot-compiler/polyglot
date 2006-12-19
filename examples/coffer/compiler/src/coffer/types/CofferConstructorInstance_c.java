/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.types.*;
import polyglot.util.*;
import java.util.*;

/** An implementation of the <code>CofferConstructorInstance</code> interface. 
 */
public class CofferConstructorInstance_c extends ConstructorInstance_c
				     implements CofferConstructorInstance
{
    protected KeySet entryKeys;
    protected KeySet returnKeys;
    protected List throwConstraints;

    public CofferConstructorInstance_c(CofferTypeSystem ts, Position pos,
	    ClassType container, Flags flags,
	    List argTypes,
            KeySet entryKeys, KeySet returnKeys, List throwConstraints)
    {
	super(ts, pos, container, flags, argTypes, Collections.EMPTY_LIST);
        this.entryKeys = entryKeys;
        this.returnKeys = returnKeys;
        this.throwConstraints = TypedList.copyAndCheck(throwConstraints, ThrowConstraint.class, true);

        if (entryKeys == null)
            throw new InternalCompilerError("null entry keys for " + this);
    }
    
    public boolean isCanonical() {
        for (Iterator i = throwConstraints.iterator(); i.hasNext(); ) {
            ThrowConstraint c = (ThrowConstraint) i.next();
            if (! c.isCanonical()) {
                return false;
            }
        }
        
        if (! entryKeys.isCanonical()) {
            return false;
        }
        
        if (returnKeys != null && ! returnKeys.isCanonical()) {
            return false;
        }
        
        return super.isCanonical();
    }

    public KeySet entryKeys() {
	return entryKeys;
    }

    public KeySet returnKeys() {
	return returnKeys;
    }

    public List throwConstraints() {
        return throwConstraints;
    }

    public List throwTypes() {
        return new CachingTransformingList(throwConstraints, new GetType());
    }

    public class GetType implements Transformation {
        public Object transform(Object o) {
            return ((ThrowConstraint) o).throwType();
        }
    }

    public void setThrowTypes(List throwTypes) {
        Iterator i = throwTypes.iterator();
        Iterator j = throwConstraints.iterator();

        List l = new LinkedList();

        while (i.hasNext() && j.hasNext()) {
            Type t = (Type) i.next();
            ThrowConstraint c = (ThrowConstraint) j.next();
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

    public void setEntryKeys(KeySet entryKeys) {
        this.entryKeys = entryKeys;
    }

    public void setReturnKeys(KeySet returnKeys) {
        this.returnKeys = returnKeys;
    }

    public void setThrowConstraints(List throwConstraints) {
        this.throwConstraints = TypedList.copyAndCheck(throwConstraints, ThrowConstraint.class, true);
    }

    public String toString() {
        return super.toString() + " " + entryKeys + "->" + returnKeys +
                                  " throws " + throwConstraints;
    }
}

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

/** An implementation of the <code>CofferMethodInstance</code> interface. 
 */
public class CofferMethodInstance_c extends MethodInstance_c
                                implements CofferMethodInstance
{
    protected KeySet entryKeys;
    protected KeySet returnKeys;
    protected List throwConstraints;

    public CofferMethodInstance_c(CofferTypeSystem ts, Position pos,
	    ReferenceType container, Flags flags, Type returnType,
	    String name, List argTypes,
            KeySet entryKeys, KeySet returnKeys, List throwConstraints)
    {
	super(ts, pos, container, flags, returnType, name, argTypes, Collections.EMPTY_LIST);

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

    public boolean canOverrideImpl(MethodInstance mj, boolean quiet) throws SemanticException {
        CofferMethodInstance mi = this;

        KeySet e;
        KeySet r;
        List l;

        if (mj instanceof CofferMethodInstance) {
            e = ((CofferMethodInstance) mj).entryKeys();
            r = ((CofferMethodInstance) mj).returnKeys();
            l = ((CofferMethodInstance) mj).throwConstraints();
        }
        else {
            CofferTypeSystem ts = (CofferTypeSystem) this.ts;
            e = ts.emptyKeySet(position());
            r = ts.emptyKeySet(position());
            l = Collections.EMPTY_LIST;
        }

        // Can pass through more keys.
        KeySet newKeys = entryKeys.removeAll(e);

        if (! returnKeys.equals(r.addAll(newKeys))) {
            if (quiet) return false;
            throw new SemanticException(mi.signature() + " in " + mi.container() +
                                        " cannot override " + 
                                        mj.signature() + " in " + mj.container() + 
                                        ";", 
                                        mi.position());
        }

        CONSTRAINTS:
        for (Iterator i = throwConstraints.iterator(); i.hasNext(); ) {
            ThrowConstraint c = (ThrowConstraint) i.next();

            for (Iterator j = l.iterator(); j.hasNext(); ) {
                ThrowConstraint superC = (ThrowConstraint) j.next();

                if (superC.throwType().equals(c.throwType())) {
                    if (! c.keys().equals(superC.keys().addAll(newKeys))) {
                        if (quiet) return false;
                        throw new SemanticException(mi.signature() + " in " + mi.container() +
                                " cannot override " + 
                                mj.signature() + " in " + mj.container() + 
                                ";", 
                                mi.position());
                    }
                    continue CONSTRAINTS;
                }
            }

            if (! c.keys().equals(newKeys)) {
                if (quiet) return false;
                throw new SemanticException(mi.signature() + " in " + mi.container() +
                        " cannot override " + 
                        mj.signature() + " in " + mj.container() + 
                        ";", 
                        mi.position());
            }
        }

        return super.canOverrideImpl(mj, quiet);
    }

    public String toString() {
        return super.toString() + " " + entryKeys + "->" + returnKeys +
                                  " throws " + throwConstraints;
    }
}

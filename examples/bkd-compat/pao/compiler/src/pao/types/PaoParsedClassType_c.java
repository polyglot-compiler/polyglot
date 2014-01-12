/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.types;

import polyglot.frontend.Source;
import polyglot.types.LazyClassInitializer;
import polyglot.types.ParsedClassType_c;
import polyglot.types.Type;
import polyglot.types.TypeSystem;

/**
 * A PAO class type. This class overrides the method 
 * {@link #isCastValidImpl(Type) isCastValidImpl(Type)} to allow casting from
 * <code>Object</code> to primitives.
 */
public class PaoParsedClassType_c extends ParsedClassType_c {
    protected PaoParsedClassType_c() {
        super();
    }

    public PaoParsedClassType_c(TypeSystem ts, LazyClassInitializer init,
            Source fromSource) {
        super(ts, init, fromSource);
    }

    /**
     * Returns <code>true</code> if normal casting rules permit this cast, or
     * if this <code>ClassType</code> is <code>Object</code> and the 
     * <code>toType</code> is a primitive.
     * 
     * @see polyglot.types.ClassType_c#isCastValidImpl(Type)
     */
    @Override
    public boolean isCastValidImpl(Type toType) {
        return toType.isPrimitive() && ts.equals(this, ts.Object())
                || super.isCastValidImpl(toType);
    }
}

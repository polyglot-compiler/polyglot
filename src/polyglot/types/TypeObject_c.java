/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 *
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.util.TypeInputStream;

/**
 * Abstract implementation of a type object.  Contains a reference to the
 * type system and to the object's position in the code.
 */
public abstract class TypeObject_c implements TypeObject {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected transient TypeSystem ts;
    protected Position position;

    /** Used for deserializing types. */
    protected TypeObject_c() {}

    /** Creates a new type in the given a TypeSystem. */
    public TypeObject_c(TypeSystem ts) {
        this(ts, null);
    }

    public TypeObject_c(TypeSystem ts, Position pos) {
        this.ts = ts;
        this.position = pos;
    }

    @Override
    public TypeObject_c copy() {
        try {
            return (TypeObject_c) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }

    @Override
    public TypeSystem typeSystem() {
        return ts;
    }

    @Override
    public Position position() {
        return position;
    }

    @SuppressWarnings("unused")
    private static final long writeObjectVersionUID = 1L;

    private void writeObject(ObjectOutputStream out) throws IOException {
        // If you update this method in an incompatible way, increment
        // writeObjectVersionUID.

        out.defaultWriteObject();
    }

    @SuppressWarnings("unused")
    private static final long readObjectVersionUID = 1L;

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // If you update this method in an incompatible way, increment
        // readObjectVersionUID.

        if (in instanceof TypeInputStream) {
            ts = ((TypeInputStream) in).getTypeSystem();
        }

        in.defaultReadObject();
    }

    /**
     * Return whether o is structurally equivalent to o.
     * Implementations should override equalsImpl().
     */
    @Override
    public final boolean equals(Object o) {
        return o instanceof TypeObject && ts.equals(this, (TypeObject) o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Default implementation is pointer equality.
     */
    @Override
    public boolean equalsImpl(TypeObject t) {
        return t == this;
    }

    /**
     * Overload equalsImpl to find inadvertent overriding errors.
     * Make package-scope and void to break callers.
     */
    final void equalsImpl(Object o) {}

    final void typeEqualsImpl(Object o) {}

    final void typeEqualsImpl(TypeObject o) {}
}

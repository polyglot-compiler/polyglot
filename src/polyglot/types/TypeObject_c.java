/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.types;

import polyglot.types.*;
import polyglot.util.*;
import java.io.*;

/**
 * Abstract implementation of a type object. Contains a reference to the type
 * system and to the object's position in the code.
 */
public abstract class TypeObject_c implements TypeObject {
	protected transient TypeSystem ts;
	protected Position position;

	/** Used for deserializing types. */
	protected TypeObject_c() {
	}

	/** Creates a new type in the given a TypeSystem. */
	public TypeObject_c(TypeSystem ts) {
		this(ts, null);
	}

	public TypeObject_c(TypeSystem ts, Position pos) {
		this.ts = ts;
		this.position = pos;
	}

	public Object copy() {
		try {
			return (TypeObject_c) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalCompilerError("Java clone() weirdness.");
		}
	}

	public TypeSystem typeSystem() {
		return ts;
	}

	public Position position() {
		return position;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		if (in instanceof TypeInputStream) {
			ts = ((TypeInputStream) in).getTypeSystem();
		}

		in.defaultReadObject();
	}

	/**
	 * Return whether o is structurally equivalent to o. Implementations should
	 * override equalsImpl().
	 */
	public final boolean equals(Object o) {
		return o instanceof TypeObject && ts.equals(this, (TypeObject) o);
	}

	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Default implementation is pointer equality.
	 */
	public boolean equalsImpl(TypeObject t) {
		return t == this;
	}

	/**
	 * Overload equalsImpl to find inadvertent overriding errors. Make
	 * package-scope and void to break callers.
	 */
	final void equalsImpl(Object o) {
	}

	final void typeEqualsImpl(Object o) {
	}

	final void typeEqualsImpl(TypeObject o) {
	}
}

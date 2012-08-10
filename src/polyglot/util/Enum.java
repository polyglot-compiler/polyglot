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

package polyglot.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** An enumerated type.  Enums are interned and can be compared with ==. */
public class Enum implements Internable, Serializable {
    /** The name of the enum.  Used for debugging and interning. */
    private String name;

    /** The intern cache. */
    private static Map<EnumKey, Enum> cache = new HashMap<EnumKey, Enum>();

    protected Enum(String name) {
        this.name = name;

        // intern the enum and make sure this one is unique
        Enum intern = internEnum();

        if (intern != this) {
            throw new InternalCompilerError("Duplicate enum \"" + name
                    + "\"; this=" + this + " (" + this.getClass().getName()
                    + "), intern=" + intern + " ("
                    + intern.getClass().getName() + ")");
        }
    }

    /** For serialization. */
    @SuppressWarnings("unused")
    private Enum() {
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public String toString() {
        return name;
    }

    private static class EnumKey {
        private Enum e;

        EnumKey(Enum e) {
            this.e = e;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof EnumKey && e.name.equals(((EnumKey) o).e.name)
                    && e.getClass() == ((EnumKey) o).e.getClass();
        }

        @Override
        public int hashCode() {
            return e.getClass().hashCode() ^ e.name.hashCode();
        }

        @Override
        public String toString() {
            return e.toString();
        }
    }

    @Override
    public Object intern() {
        return internEnum();
    }

    public Enum internEnum() {
        EnumKey k = new EnumKey(this);

        Enum e = cache.get(k);

        if (e == null) {
            cache.put(k, this);
            return this;
        }

        return e;
    }
}

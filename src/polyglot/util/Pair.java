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

/** A two-element tuple.
 */
public class Pair<T, U> {
    protected T part1;
    protected U part2;

    public Pair(T p1, U p2) {
        this.part1 = p1;
        this.part2 = p2;
    }

    public T part1() {
        return part1;
    }

    public U part2() {
        return part2;
    }

    @Override
    public String toString() {
        return "(" + part1 + ", " + part2 + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Pair) {
            @SuppressWarnings("rawtypes")
            Pair p = (Pair) o;
            return (part1 == null ? p.part1 == null : part1.equals(p.part1))
                    && (part2 == null ? p.part2 == null : part2.equals(p.part2));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (part1 != null ? part1.hashCode() : 0)
                ^ (part2 != null ? part2.hashCode() : 0);
    }
}

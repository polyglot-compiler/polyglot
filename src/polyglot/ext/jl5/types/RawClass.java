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
package polyglot.ext.jl5.types;

/**
 * Represents a raw class. See JLS 3rd ed., 4.8.
 *
 */
public interface RawClass extends JL5ClassType {
    /**
     * The JL5ParsedClassType of which this is the raw version. This 
     * JL5ParsedClassType will have at least one type parameter.
     * 
     */
    JL5ParsedClassType base();

    /**
     * Return the JL5SubstClassType erased version of the raw class. This should be used very cautiously,
     * as the erased class type is not the same as the raw type. For example, given
     * class C<T extends D>, the raw class is |C|, which is not the same in the Polyglot type system
     * as the class C<D>, which is the erased version of the class.
     */
    JL5SubstClassType erased();
}

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

/**
 * A {@code Named} is a TypeObject that is named.
 */
public interface Named extends TypeObject {
    /**
     * Simple name of the type object. Anonymous classes do not have names.
     */
    String name();

    /**
     * Full dotted-name of the type object. For a package, top level class, 
     * top level interface, or primitive type, this is
     * the fully qualified name. For a member class or interface that is
     * directly enclosed in a class or interface with a fully qualified name,
     * then this is the fully qualified name of the member class or interface. 
     * For local and anonymous classes, this method returns a string that is
     * not the fully qualified name (as these classes do not have fully 
     * qualified names), but that may be suitable for debugging or error 
     * messages. 
     */
    String fullName();
}

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

package polyglot.ast;

import java.util.List;

/**
 * A {@code ClassBody} represents the body of a class or interface
 * declaration or the body of an anonymous class.
 */
public interface ClassBody extends Term {
    /**
     * List of the class's members.
     * @return A list of {@link polyglot.ast.ClassMember ClassMember}.
     */
    List<ClassMember> members();

    /**
     * Set the class's members.
     * @param members A list of {@link polyglot.ast.ClassMember ClassMember}.
     */
    ClassBody members(List<ClassMember> members);

    /**
     * Add a member to the class, returning a new node.
     */
    ClassBody addMember(ClassMember member);
}

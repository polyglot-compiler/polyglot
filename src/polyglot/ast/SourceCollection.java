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
 * A {@code SourceCollection} represents a collection of source files.
 * This node should be used only during AST rewriting, just before code
 * generation in order to generate multiple target files from a single
 * AST.
 */
public interface SourceCollection extends Node {
    /** List of source files in the collection.
     * @return A list of {@link polyglot.ast.SourceFile SourceFile}.
     */
    List<SourceFile> sources();

    /** Set the list of source files in the collection.
     * @param sources A list of {@link polyglot.ast.SourceFile SourceFile}.
     */
    SourceCollection sources(List<SourceFile> sources);
}

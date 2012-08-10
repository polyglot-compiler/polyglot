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

package polyglot.ast;

import polyglot.types.ImportTable;
import polyglot.frontend.Source;
import java.util.List;

/**
 * A <code>SourceFile</code> is an immutable representations of a Java
 * language source file.  It consists of a package name, a list of 
 * <code>Import</code>s, and a list of <code>GlobalDecl</code>s.
 */
public interface SourceFile extends Node {
    /** Get the source's declared package. */
    PackageNode package_();

    /** Set the source's declared package. */
    SourceFile package_(PackageNode package_);

    /** Get the source's declared imports.
     * @return A list of {@link polyglot.ast.Import Import}.
     */
    List<Import> imports();

    /** Set the source's declared imports.
     * @param imports A list of {@link polyglot.ast.Import Import}.
     */
    SourceFile imports(List<Import> imports);

    /** Get the source's top-level declarations.
     * @return A list of {@link polyglot.ast.TopLevelDecl TopLevelDecl}.
     */
    List<TopLevelDecl> decls();

    /** Set the source's top-level declarations.
     * @param decls A list of {@link polyglot.ast.TopLevelDecl TopLevelDecl}.
     */
    SourceFile decls(List<TopLevelDecl> decls);

    /** Get the source's import table. */
    ImportTable importTable();

    /** Set the source's import table. */
    SourceFile importTable(ImportTable importTable);

    /** Get the source file. */
    Source source();

    /** Set the source file. */
    SourceFile source(Source source);
}

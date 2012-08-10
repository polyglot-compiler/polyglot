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

import polyglot.main.Options;
import polyglot.types.Named;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.StringUtil;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An <code>Import</code> is an immutable representation of a Java
 * <code>import</code> statement.  It consists of the string representing the
 * item being imported and the kind which is either indicating that a class
 * is being imported, or that an entire package is being imported.
 */
public class Import_c extends Node_c implements Import {
    protected Kind kind;
    protected String name;

    public Import_c(Position pos, Kind kind, String name) {
        super(pos);
        assert (kind != null && name != null);
        this.name = name;
        this.kind = kind;
    }

    /** Get the name of the import. */
    @Override
    public String name() {
        return this.name;
    }

    /** Set the name of the import. */
    @Override
    public Import name(String name) {
        Import_c n = (Import_c) copy();
        n.name = name;
        return n;
    }

    /** Get the kind of the import. */
    @Override
    public Kind kind() {
        return this.kind;
    }

    /** Set the kind of the import. */
    @Override
    public Import kind(Kind kind) {
        Import_c n = (Import_c) copy();
        n.kind = kind;
        return n;
    }

    /**
     * Build type objects for the import.
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
    ImportTable it = tb.importTable();

    if (kind == CLASS) {
        it.addClassImport(name);
    }
    else if (kind == PACKAGE) {
        it.addPackageImport(name);
    }

    return this;
    }
     */

    /** Check that imported classes and packages exist. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        if (kind == PACKAGE && tc.typeSystem().packageExists(name)) {
            return this;
        }

        // Must be importing a class, either as p.C, or as p.C.*

        // The first component of the type name must be a package.
        String pkgName = StringUtil.getFirstComponent(name);

        if (!tc.typeSystem().packageExists(pkgName)) {
            throw new SemanticException("Package \"" + pkgName
                    + "\" not found.", position());
        }

        // The type must exist.
        Named nt = tc.typeSystem().forName(name);

        // And the type must be accessible.
        if (nt instanceof Type) {
            Type t = (Type) nt;
            if (t.isClass()) {
                tc.typeSystem().classAccessibleFromPackage(t.toClass(),
                                                           tc.context()
                                                             .package_());
            }
        }

        return this;
    }

    @Override
    public String toString() {
        return "import " + name + (kind == PACKAGE ? ".*" : "");
    }

    /** Write the import to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (!Options.global.fully_qualified_names) {
            w.write("import ");
            w.write(name);

            if (kind == PACKAGE) {
                w.write(".*");
            }

            w.write(";");
            w.newline(0);
        }
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Import(this.position, this.kind, this.name);
    }

}

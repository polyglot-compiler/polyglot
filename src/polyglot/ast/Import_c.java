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

import polyglot.main.Options;
import polyglot.types.Named;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.util.StringUtil;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * An {@code Import} is an immutable representation of a Java
 * {@code import} statement.  It consists of the string representing the
 * item being imported and the kind which is either indicating that a class
 * is being imported, or that an entire package is being imported.
 */
public class Import_c extends Node_c implements Import {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Kind kind;
    protected String name;

//    @Deprecated
    public Import_c(Position pos, Kind kind, String name) {
        this(pos, kind, name, null);
    }

    public Import_c(Position pos, Kind kind, String name, Ext ext) {
        super(pos, ext);
        assert (kind != null && name != null);
        this.name = name;
        this.kind = kind;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Import name(String name) {
        return name(this, name);
    }

    protected <N extends Import_c> N name(N n, String name) {
        if (n.name == name) return n;
        n = copyIfNeeded(n);
        n.name = name;
        return n;
    }

    @Override
    public Kind kind() {
        return this.kind;
    }

    @Override
    public Import kind(Kind kind) {
        return kind(this, kind);
    }

    protected <N extends Import_c> N kind(N n, Kind kind) {
        if (n.kind == kind) return n;
        n = copyIfNeeded(n);
        n.kind = kind;
        return n;
    }

    @Override
    public Node buildTypes(TypeBuilder tb) {
        Import n = this;
        if (n.kind() == Import.SINGLE_TYPE) {
            tb.importTable().addClassImport(n.name(), n.position());
        }
        else if (n.kind() == Import.TYPE_IMPORT_ON_DEMAND) {
            tb.importTable().addTypeOnDemandImport(n.name(), n.position());
        }
        return n;
    }

    /** Check that imported classes and packages exist. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        if (kind == TYPE_IMPORT_ON_DEMAND
                && tc.typeSystem().packageExists(name)) {
            // we're importing a package on demand.
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

        // The type must be canonical.
        String fullName = nt.fullName();
        if (!fullName.equals(name)) {
            throw new SemanticException("The imported type " + name
                    + " is not canonical; use " + fullName + " instead.");
        }

        // And the type must be accessible.
        if (nt instanceof Type) {
            Type t = (Type) nt;
            if (t.isClass()
                    && !tc.typeSystem()
                          .classAccessibleFromPackage(t.toClass(),
                                                      tc.context().package_())) {
                throw new SemanticException("The imported type " + t
                        + " is not visible.");
            }
        }

        return this;
    }

    @Override
    public String toString() {
        return "import " + name + (kind == TYPE_IMPORT_ON_DEMAND ? ".*" : "");
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (!Options.global.fully_qualified_names) {
            w.write("import ");
            w.write(name);

            if (kind == TYPE_IMPORT_ON_DEMAND) {
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

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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import polyglot.main.Report;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;

/** A class resolver implemented as a map from names to types. */
public class TableResolver implements TopLevelResolver {
    protected Map<String, Named> table;

    /**
     * Create a resolver.
     */
    public TableResolver() {
        this.table = new HashMap<String, Named>();
    }

    /**
     * Add a named type object to the table.
     */
    public void addNamed(Named type) {
        addNamed(type.name(), type);
    }

    /**
     * Add a named type object to the table.
     */
    public void addNamed(String name, Named type) {
        if (name == null || type == null) {
            throw new InternalCompilerError("Bad insertion into TableResolver");
        }
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "TableCR.addNamed(" + name + ", " + type + ")");
        table.put(name, type);
    }

    @Override
    public boolean packageExists(String name) {
        /* Check if a package exists in the table. */
        for (Named type : table.values()) {
            if (type instanceof Importable) {
                Importable im = (Importable) type;
                if (im.package_() != null
                        && (im.package_().fullName().equals(name) || im.package_()
                                                                       .fullName()
                                                                       .startsWith(name
                                                                               + "."))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Find a type by name.
     */
    @Override
    public Named find(String name) throws SemanticException {
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "TableCR.find(" + name + ")");

        Named n = table.get(name);

        if (n != null) {
            return n;
        }

        throw new NoClassException(name);
    }

    @Override
    public String toString() {
        return "(table " + table + ")";
    }

    private static final Collection<String> TOPICS =
            CollectionUtil.list(Report.types, Report.resolver);
}

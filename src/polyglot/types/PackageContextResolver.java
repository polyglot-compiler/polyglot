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

import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;

/**
 * A <code>PackageContextResolver</code> is responsible for looking up types
 * and packages in a package by name.
 */
public class PackageContextResolver extends AbstractAccessControlResolver {
    protected Package p;

    /**
     * Create a package context resolver.
     * @param ts The type system.
     * @param p The package in whose context to search.
     */
    public PackageContextResolver(TypeSystem ts, Package p) {
        super(ts);
        this.p = p;
    }

    /**
     * The package in whose context to search.
     */
    public Package package_() {
        return p;
    }

    /**
     * The system resolver.
     */
    public Resolver outer() {
        return ts.systemResolver();
    }

    /**
     * Find a type object by name.
     */
    @Override
    public Named find(String name, ClassType accessor) throws SemanticException {
        if (!StringUtil.isNameShort(name)) {
            throw new InternalCompilerError("Cannot lookup qualified name "
                    + name);
        }

        Named n = null;

        try {
            n = ts.systemResolver().find(p.fullName() + "." + name);
        }
        catch (NoClassException e) {
            // Rethrow if some _other_ class or package was not found.
            if (!e.getClassName().equals(p.fullName() + "." + name)) {
                throw e;
            }
        }

        if (n == null) {
            n = ts.createPackage(p, name);
        }

        if (!canAccess(n, accessor)) {
            throw new SemanticException("Cannot access " + n + " from "
                    + accessor + ".");
        }

        return n;
    }

    protected boolean canAccess(Named n, ClassType accessor) {
        if (n instanceof ClassType) {
            return accessor == null
                    || ts.classAccessible((ClassType) n, accessor);
        }
        return true;
    }

    @Override
    public String toString() {
        return "(package-context " + p.toString() + ")";
    }
}

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

import polyglot.util.CodeWriter;

/**
 * An <code>PackageType</code> represents a package type. It may or may
 * not be fully qualified. Package types are never canonical and never
 * primitive.
 */
public class Package_c extends TypeObject_c implements Package {
    protected Package prefix;
    protected String name;
    /**
     * The full name is computed lazily from the prefix and name.
     */
    protected String fullname = null;

    /** Used for deserializing types. */
    protected Package_c() {
    }

    public Package_c(TypeSystem ts) {
        this(ts, null, null);
    }

    public Package_c(TypeSystem ts, String name) {
        this(ts, null, name);
    }

    public Package_c(TypeSystem ts, Package prefix, String name) {
        super(ts);
        this.prefix = prefix;
        this.name = name;
        this.decl = this;
    }

    protected transient Resolver memberCache;

    @Override
    public Resolver resolver() {
        if (memberCache == null) {
            memberCache =
                    new CachingResolver(ts.createPackageContextResolver(this));
        }
        return memberCache;
    }

    @Override
    public Package_c copy() {
        Package_c n = (Package_c) super.copy();
        n.memberCache = null;
        return n;
    }

    protected Package decl;

    @Override
    public Declaration declaration() {
        return decl;
    }

    @Override
    public void setDeclaration(Declaration decl) {
        this.decl = (Package) decl;
    }

    @Override
    public boolean equalsImpl(TypeObject o) {
        if (o instanceof Package) {
            Package p = (Package) o;
            if (name.equals(p.name())) {
                if (prefix == null)
                    return p.prefix() == null;
                else return ts.equals(prefix, p.prefix());
            }
        }
        return false;
    }

    @Override
    public final boolean packageEquals(Package p) {
        return ts.packageEquals(this, p);
    }

    @Override
    public boolean packageEqualsImpl(Package p) {
        if (name.equals(p.name())) {
            if (prefix == null)
                return p.prefix() == null;
            else return ts.packageEquals(prefix, p.prefix());
        }
        return false;
    }

    @Override
    public boolean isType() {
        return false;
    }

    @Override
    public boolean isPackage() {
        return true;
    }

    @Override
    public Type toType() {
        return null;
    }

    @Override
    public Package toPackage() {
        return this;
    }

    @Override
    public Package prefix() {
        return prefix;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String translate(Resolver c) {
        if (prefix() == null) {
            return name();
        }

        return prefix().translate(c) + "." + name();
    }

    @Override
    public String fullName() {
        if (fullname == null) {
            fullname =
                    prefix() == null ? name : prefix().fullName() + "." + name;
        }
        return fullname;
    }

    @Override
    public String toString() {
        return prefix() == null ? name : prefix().toString() + "." + name;
    }

    @Override
    public void print(CodeWriter w) {
        if (prefix() != null) {
            prefix().print(w);
            w.write(".");
            w.allowBreak(2, 3, "", 0);
        }
        w.write(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean isCanonical() {
        return true;
    }
}

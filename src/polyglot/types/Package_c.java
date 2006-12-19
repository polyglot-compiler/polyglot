/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.types;

import polyglot.types.*;
import polyglot.types.Package;
import polyglot.util.CodeWriter;

/**
 * An <code>PackageType</code> represents a package type. It may or may
 * not be fully qualified. Package types are never canonical and never
 * primitive.
 */
public class Package_c extends TypeObject_c implements Package
{
    protected Package prefix;
    protected String name;
    /**
     * The full name is computed lazily from the prefix and name.
     */
    protected String fullname = null;

    /** Used for deserializing types. */
    protected Package_c() { }
    
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
    
    public Resolver resolver() {
        if (memberCache == null) {
            memberCache = new CachingResolver(ts.createPackageContextResolver(this));
        }
        return memberCache;
    }
    
    public Object copy() {
        Package_c n = (Package_c) super.copy();
        n.memberCache = null;
        return n;
    }
    
    protected Package decl;
    
    public Declaration declaration() {
        return decl;
    }
    
    public void setDeclaration(Declaration decl) {
        this.decl = (Package) decl;        
    }
    
    public boolean equalsImpl(TypeObject o) {
        if (o instanceof Package) {
            Package p = (Package) o;
            if (name.equals(p.name())) {
                if (prefix == null)
                    return p.prefix() == null;
                else
                    return ts.equals(prefix, p.prefix());
            }
        }
        return false;
    }
        
    public final boolean packageEquals(Package p) {
        return ts.packageEquals(this, p);
    }
    
    public boolean packageEqualsImpl(Package p) {
        if (name.equals(p.name())) {
            if (prefix == null)
                return p.prefix() == null;
            else
                return ts.packageEquals(prefix, p.prefix());
        }
        return false;
    }

    public boolean isType() { return false; }
    public boolean isPackage() { return true; }
    public Type toType() { return null; }
    public Package toPackage() { return this; }

    public Package prefix() {
	return prefix;
    }

    public String name() {
	return name;
    }

    public String translate(Resolver c) {
        if (prefix() == null) {
          return name();
        }

        return prefix().translate(c) + "." + name();
    }

    public String fullName() {
        if (fullname == null) {
            fullname = prefix() == null ? name : prefix().fullName() + "." + name;
        }
        return fullname;
    }

    public String toString() {
	return prefix() == null ? name : prefix().toString() + "." + name;
    }
    public void print(CodeWriter w) {
	if (prefix() != null) {
	    prefix().print(w);
	    w.write(".");
	    w.allowBreak(2, 3, "", 0);
	}
	w.write(name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean isCanonical() {
	return true;
    }
}

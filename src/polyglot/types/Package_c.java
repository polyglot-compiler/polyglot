package jltools.ext.jl.types;

import jltools.types.*;
import jltools.types.Package;
import jltools.util.*;

/**
 * An <code>PackageType</code> represents a package type. It may or may
 * not be fully qualified. Package types are never canonical and never
 * primitive.
 */
public class Package_c extends TypeObject_c implements Package
{
    protected Package prefix;
    protected String name;

    /** Used for deserializing types. */
    protected Package_c() { }
    
    public Package_c(TypeSystem ts, String name) {
        this(ts, null, name);
    }

    public Package_c(TypeSystem ts, Package prefix, String name) {
        super(ts);
	this.prefix = prefix;
	this.name = name;
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
    
    public String translate(Context c) {
	return ts.translatePackage(c, this);
    }

    public String fullName() {
	return prefix == null ? name : prefix.fullName() + "." + name;
    }

    public String toString() {
	return prefix == null ? name : prefix.toString() + "." + name;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof Package) {
	    Package p = (Package) o;
	    if (prefix == null) {
		return p.prefix() == null && name.equals(p.name());
	    }
	    else {
		return prefix.equals(p.prefix()) && name.equals(p.name());
	    }
	}

	return false;
    }

    public boolean isCanonical() {
	return true;
    }
}

package polyglot.types;

import java.util.Iterator;
import java.util.List;
import polyglot.ast.*;
import polyglot.util.*;
import polyglot.types.Package;

/**
 * A PackageContextResolver is responsible for looking up types and packages
 * in a packge by name.
 **/
public class PackageContextResolver implements Resolver
{
    Package p;
    TypeSystem ts;
    Resolver cr;

    public PackageContextResolver(TypeSystem ts, Package p, Resolver cr) {
	this.ts = ts;
	this.p = p;
	this.cr = cr;
    }

    public Package package_() {
        return p;
    }

    public Type findType(String name) throws SemanticException {
        Qualifier q = findQualifier(name);

	if (q.isType()) {
	    return q.toType();
	}

	throw new NoClassException("Could not find type " + name +
				   " in package " + p + ".");
    }

    public Qualifier findQualifier(String name) throws SemanticException {
	if (! StringUtil.isNameShort(name)) {
	    throw new InternalCompilerError(
		"Cannot lookup qualified name " + name);
	}

        if (cr == null) {
	    return ts.packageForName(p, name);
        }

	try {
	    return cr.findType(p.fullName() + "." + name);
	}
	catch (NoClassException e) {
	    return ts.packageForName(p, name);
	}
    }

    public String toString() {
        return "(package-context " + p.toString() + ")";
    }
}

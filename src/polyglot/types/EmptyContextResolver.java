package jltools.types;

import java.util.Iterator;
import java.util.List;
import jltools.ast.*;
import jltools.util.*;

/**
 * An EmptyContextResolver is responsible for looking up types and packages
 * by name in an empty context.
 */
public class EmptyContextResolver implements Resolver {
    transient TypeSystem ts;
    transient Resolver cr;

    public EmptyContextResolver(TypeSystem ts, Resolver cr) {
	this.ts = ts;
	this.cr = cr;
    }

    public Type findType(String name) throws SemanticException {
        Qualifier q = findQualifier(name);

	if (q.isType()) {
	    return q.toType();
	}

	throw new NoClassException("Could not find type " + name + ".");
    }

    public Qualifier findQualifier(String name) throws SemanticException {
	if (! StringUtil.isNameShort(name)) {
	    throw new InternalCompilerError(
		"Cannot lookup qualified name " + name);
	}

	try {
	    return cr.findType(name);
	}
	catch (NoClassException e) {
	    return ts.packageForName(name);
	}
    }

    public String toString() {
        return "(empty-context " + cr.getClass().getName() + ")";
    }
}

package jltools.types;

import jltools.ast.*;
import jltools.util.*;
import java.util.*;

/**
 * A <code>ClassContextResolver</code> looks up type names qualified with a class name.
 * For example, if the class is "A.B", the class context will return the class
 * for member class "A.B.C" (if it exists) when asked for "C".
 */
public class ClassContextResolver extends ClassResolver {
    TypeSystem ts;
    ClassType type;

    public ClassContextResolver(TypeSystem ts, ClassType type) {
	this.ts = ts;
	this.type = type;
    }

    public String toString() {
        return "(class-context " + type + ")";
    }

    private static Set visited = new HashSet();

    public Type findType(String name) throws SemanticException {
        Types.report(1, "Looking for " + name + " in " + this);

	Object key = new Integer(name.hashCode() + type.hashCode());
	if (visited.contains(key))
	    throw new InternalCompilerError("Cycle while looking up \"" + name + "\" in " + type + ".");
	visited.add(key);

	try {
	    if (! StringUtil.isNameShort(name)) {
		throw new InternalCompilerError(
		    "Cannot lookup qualified name " + name);
	    }

	    // Check if the name is for this class.
	    if (type instanceof NamedType) {
		String typeName = ((NamedType) type).name();

		if (name.equals(typeName)) {
		    return type;
		}
	    }

	    // Check if the name is for a member class.
	    MemberClassType inner = type.memberClassNamed(name);

	    if (inner != null) {
		return inner;
	    }

	    // Check super types and enclosing types.
	    Set found = new HashSet();

	    Type superType = type.superType();

	    if (superType != null && superType.isClass()) {
		try {
		    Resolver cc = ts.classContextResolver(superType.toClass());
		    Type t = cc.findType(name);
		    found.add(t);
		}
		catch (NoClassException e) {
		}
	    }

	    // FIXME: Is this right?
	    for (Iterator i = type.interfaces().iterator(); i.hasNext(); ) {
		Type it = (Type) i.next();

		if (it.isClass()) {
		    try {
			Resolver cc = ts.classContextResolver(it.toClass());
			Type t = cc.findType(name);
			found.add(t);
		    }
		    catch (NoClassException e) {
		    }
		}
	    }

	    if (type.isInner()) {
		try {
		    Resolver cc = ts.classContextResolver(type.toInner().outer());
		    Type t = cc.findType(name);
		    found.add(t);
		}
		catch (NoClassException e) {
		}
	    }

	    if (found.size() == 1) {
		return (Type) new ArrayList(found).get(0);
	    }

	    if (found.size() > 1) {
		throw new SemanticException("Duplicate classes " + name +
		    " found in scope of " + type + ": " + found);
	    }

	    throw new NoClassException("Could not find type " + name +
		" in scope of " + type + ".");
	}
	finally {
	    visited.remove(key);
	}
    }

    public ClassType classType() {
	return type;
    }
}

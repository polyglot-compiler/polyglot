package jltools.types;

import jltools.util.*;
import java.util.*;

/** A class resolver implemented as a map from names to types. */
public class TableResolver extends ClassResolver
{
    protected Map table;

    public TableResolver() {
	this.table = new HashMap();
    }

    public void addType(NamedType type) {
        if (type == null) {
            throw new InternalCompilerError("Bad insertion into TableResolver");
        }
	Types.report(1, "TableCR.addType(" + type + ")");
	table.put(type.name(), type);
    }

    public void addType(String name, NamedType type) {
        if (name == null || type == null) {
            throw new InternalCompilerError("Bad insertion into TableResolver");
        }
	Types.report(1, "TableCR.addType(" + name + ", " + type + ")");
	table.put(name, type);
    }

    public Type findType(String name) throws SemanticException {
	Types.report(1, "TableCR.findType(" + name + ")");

	Type type = (Type) table.get(name);

	if (type != null) {
	    return type;
	}

	throw new NoClassException("Could not find type " + name +
	    " in context.");
    }

    public String toString() {
        return "(table " + table + ")";
    }
}

package polyglot.types;

import polyglot.util.*;
import java.util.*;

/** A class resolver implemented as a map from names to types. */
public class TableResolver extends ClassResolver {
    protected Map table;

    /**
     * Create a resolver.
     */
    public TableResolver() {
	this.table = new HashMap();
    }

    /**
     * Add a type to the table.
     */
    public void addType(NamedType type) {
        addType(type.name(), type);
    }

    /**
     * Add a type to the table.
     */
    public void addType(String name, NamedType type) {
        if (name == null || type == null) {
            throw new InternalCompilerError("Bad insertion into TableResolver");
        }
	Types.report(1, "TableCR.addType(" + name + ", " + type + ")");
	table.put(name, type);
    }

    /**
     * Find a type by name.
     */
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

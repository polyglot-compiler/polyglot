package polyglot.types;

import polyglot.util.*;
import polyglot.main.Report;
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

    /**
     * Find a type by name.
     */
    public Named find(String name) throws SemanticException {
        if (Report.should_report(TOPICS, 3))
	    Report.report(3, "TableCR.find(" + name + ")");

	Named n = (Named) table.get(name);

	if (n != null) {
	    return n;
	}

	throw new NoClassException(name);
    }

    public String toString() {
        return "(table " + table + ")";
    }
    
    private static final Collection TOPICS = 
                CollectionUtil.list(Report.types, Report.resolver);
}
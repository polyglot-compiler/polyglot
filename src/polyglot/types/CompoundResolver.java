package polyglot.types;

import polyglot.ast.*;
import polyglot.util.*;
import java.util.*;

/**
 * An <code>CompoundResolver</code> resolves names using more than one
 * context.
 */
public class CompoundResolver implements Resolver {
    Resolver head;
    Resolver tail;

    public CompoundResolver(Resolver head, Resolver tail) {
	this.head = head;
	this.tail = tail;
    }

    public String toString() {
        return "(compound " + head + " " + tail + ")";
    }

    public Qualifier findQualifier(String name) throws SemanticException {
	try {
	    return head.findQualifier(name);
	}
	catch (NoClassException e) {
	    return tail.findQualifier(name);
	}
    }

    public Type findType(String name) throws SemanticException {
	try {
	    return head.findType(name);
	}
	catch (NoClassException e) {
	    return tail.findType(name);
	}
    }
}

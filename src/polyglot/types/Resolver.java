package polyglot.types;

import java.util.Iterator;
import java.util.List;
import polyglot.ast.*;

/**
 * A <code>Resolver</code> is responsible for looking up types and
 * packages by name.
 */
public interface Resolver {

    /**
     * Find a qualifier by name.
     */
    public Qualifier findQualifier(String name) throws SemanticException;

    /**
     * Find a type by name.
     */
    public Type findType(String name) throws SemanticException;
}

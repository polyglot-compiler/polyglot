package jltools.types;

import java.util.Iterator;
import java.util.List;
import jltools.ast.*;

/**
 * A <code>Resolver</code> is responsible for looking up types and
 * packages by name.
 */
public interface Resolver {
    public Qualifier findQualifier(String name) throws SemanticException;
    public Type findType(String name) throws SemanticException;
}

package jltools.ast;

import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * Utility class which is used to disambiguate ambiguous
 * AST nodes (Expr, Type, Receiver, Qualifier, Prefix).
 */
public interface Disamb
{
    /**
	 * Disambiguate the prefix and name into a unambiguous node type.
     * @return An unambiguous AST node, or null if disambiguation
     * 		   fails.
	 */
    Node disambiguate(SemanticVisitor v, Position pos,
			Prefix prefix, String name) throws SemanticException;

}



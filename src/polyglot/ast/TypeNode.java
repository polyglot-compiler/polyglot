package jltools.ast;

import jltools.types.Type;
import jltools.types.SemanticException;

/**
 * A <code>TypeNode</code> is the syntactic representation of a 
 * <code>Type</code> within the abstract syntax tree.
 */
public interface TypeNode extends Receiver, QualifierNode
{
    Type type();
    TypeNode type(Type type);
}

package polyglot.ast;

import polyglot.types.Type;
import polyglot.types.SemanticException;

/**
 * A <code>TypeNode</code> is the syntactic representation of a 
 * <code>Type</code> within the abstract syntax tree.
 */
public interface TypeNode extends Receiver, QualifierNode
{
    TypeNode type(Type type);
}

package carray.ast;

import polyglot.ast.NodeFactory;
import polyglot.ast.TypeNode;
import polyglot.util.Position;

/**
 * NodeFactory for carray extension.
 *
 */
public interface CarrayNodeFactory extends NodeFactory {
    public ConstArrayTypeNode ConstArrayTypeNode(Position pos, TypeNode base);
}

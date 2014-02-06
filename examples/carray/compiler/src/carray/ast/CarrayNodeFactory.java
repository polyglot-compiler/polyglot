package carray.ast;

import polyglot.ast.ArrayTypeNode;
import polyglot.ast.NodeFactory;
import polyglot.ast.TypeNode;
import polyglot.util.Position;

/**
 * NodeFactory for carray extension.
 *
 */
public interface CarrayNodeFactory extends NodeFactory {
    ArrayTypeNode ConstArrayTypeNode(Position pos, TypeNode base);
}

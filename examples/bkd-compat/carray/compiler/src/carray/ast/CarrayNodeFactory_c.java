package carray.ast;

import polyglot.ast.ArrayAccess;
import polyglot.ast.ArrayAccessAssign;
import polyglot.ast.Assign;
import polyglot.ast.Expr;
import polyglot.ast.NodeFactory_c;
import polyglot.ast.TypeNode;
import polyglot.util.Position;

/**
 * NodeFactory for carray extension.
 *
 */
public class CarrayNodeFactory_c extends NodeFactory_c implements
        CarrayNodeFactory {
    @Override
    public ConstArrayTypeNode ConstArrayTypeNode(Position pos, TypeNode base) {
        return new ConstArrayTypeNode_c(pos, base);
    }

    @Override
    public ArrayAccessAssign ArrayAccessAssign(Position pos, ArrayAccess left,
            Assign.Operator op, Expr right) {
        return new CarrayAssign_c(pos, left, op, right);
    }

}

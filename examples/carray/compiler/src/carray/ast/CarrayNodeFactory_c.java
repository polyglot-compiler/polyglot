package polyglot.ext.carray.ast;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.types.Flags;
import polyglot.types.Package;
import polyglot.types.Type;
import polyglot.types.Qualifier;
import polyglot.util.*;
import java.util.*;

/**
 * NodeFactory for carray extension.
 *
 */
public class CarrayNodeFactory_c extends NodeFactory_c implements CarrayNodeFactory {
    public ConstArrayTypeNode ConstArrayTypeNode(Position pos, TypeNode base) {
        return new ConstArrayTypeNode_c(pos, base);
    }
    public Assign Assign(Position pos, Expr left, Assign.Operator op, Expr right) {
        return new CarrayAssign_c(pos, left, op, right);
    }

}

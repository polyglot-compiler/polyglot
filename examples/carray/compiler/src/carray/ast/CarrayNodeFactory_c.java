package jltools.ext.carray.ast;

import jltools.ast.*;
import jltools.ext.jl.ast.*;
import jltools.types.Flags;
import jltools.types.Package;
import jltools.types.Type;
import jltools.types.Qualifier;
import jltools.util.*;
import java.util.*;

/**
 * NodeFactory for carray extension.
 *
 */
public class CarrayNodeFactory_c extends NodeFactory_c implements CarrayNodeFactory {
    public ConstArrayTypeNode ConstArrayTypeNode(Position pos, TypeNode base) {
        return new ConstArrayTypeNode_c(new Ext_c(), pos, base);
    }
    public Assign Assign(Position pos, Expr left, Assign.Operator op, Expr right) {
        return new CarrayAssign_c(new Ext_c(), pos, left, op, right);
    }

}

package polyglot.ext.pao.ast;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.types.*;
import polyglot.frontend.*;
import polyglot.visit.*;
import polyglot.util.*;

public class PaoInstanceof_c extends Instanceof_c { 
  public PaoInstanceof_c(Del ext, Position pos, Expr expr, TypeNode tn) {
    super(ext, pos, expr, tn);
  }

  public Node typeCheck(TypeChecker tc) throws SemanticException {
    Type rtype = compareType.type();
    Type ltype = expr.type();

    if (! tc.typeSystem().isCastValid(ltype, rtype)) {
      throw new SemanticException(
                 "Left operand of \"instanceof\" must be castable to "
                 + "the right operand.");
    }

    return type(tc.typeSystem().Boolean());
  }
}

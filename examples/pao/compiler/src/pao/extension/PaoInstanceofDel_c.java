package polyglot.ext.pao.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.pao.ast.*;
import polyglot.ext.pao.types.*;
import polyglot.types.*;
import polyglot.frontend.*;
import polyglot.visit.*;
import polyglot.util.*;

public class PaoInstanceofDel_c extends JL_c {
  public Node typeCheck(TypeChecker tc) throws SemanticException {
      Instanceof n = (Instanceof) node();
      Type rtype = n.compareType().type();
      Type ltype = n.expr().type();

      if (! tc.typeSystem().isCastValid(ltype, rtype)) {
          throw new SemanticException(
                    "Left operand of \"instanceof\" must be castable to "
                    + "the right operand.");
      }

      return n.type(tc.typeSystem().Boolean());
  }
}

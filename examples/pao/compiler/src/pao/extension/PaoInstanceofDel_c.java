package polyglot.ext.pao.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.pao.ast.*;
import polyglot.ext.pao.types.*;
import polyglot.types.*;
import polyglot.frontend.*;
import polyglot.visit.*;
import polyglot.util.*;

public class PaoInstanceofDel_c extends PaoDel_c {
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

  public Node rewrite(PaoTypeSystem ts, PaoNodeFactory nf) {
      Instanceof n = (Instanceof) node();
      Type rtype = n.compareType().type();

      if (rtype.isPrimitive()) {
          ConstructorInstance ci = ts.wrapper(rtype.toPrimitive());
          return n.compareType(nf.CanonicalTypeNode(n.compareType().position(),
                                                    ci.container()));
      }

      return n;
  }
}

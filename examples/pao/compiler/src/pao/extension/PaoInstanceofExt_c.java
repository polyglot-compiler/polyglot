package polyglot.ext.pao.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.pao.ast.*;
import polyglot.ext.pao.types.*;
import polyglot.types.*;
import polyglot.frontend.*;
import polyglot.visit.*;
import polyglot.util.*;

public class PaoInstanceofExt_c extends PaoExt_c {
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

package polyglot.ext.pao.ast;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.pao.extension.*;
import polyglot.util.*;
import java.util.*;

/**
 * NodeFactory for pao extension.
 */
public class PaoNodeFactory_c extends NodeFactory_c {
    public PaoNodeFactory_c() {
        super(new PaoExtFactory_c());
    }
    protected PaoNodeFactory_c(ExtFactory extFact) {
        super(extFact);
    }

    public Instanceof Instanceof(Position pos, Expr expr, TypeNode type) {
        Instanceof n = new Instanceof_c(pos, expr, type);
        n = (Instanceof)n.ext(extFactory().extInstanceof());
        return (Instanceof)n.del(new PaoInstanceofDel_c());
    }
}

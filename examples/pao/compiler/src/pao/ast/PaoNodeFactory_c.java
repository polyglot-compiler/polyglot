package polyglot.ext.pao.ast;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.util.*;
import java.util.*;

/**
 * NodeFactory for pao extension.
 */
public class PaoNodeFactory_c extends NodeFactory_c implements PaoNodeFactory {
    public Instanceof Instanceof(Position pos, Expr expr, TypeNode type) {
	return new PaoInstanceof_c(new Del_c(), pos, expr, type);
    }
}

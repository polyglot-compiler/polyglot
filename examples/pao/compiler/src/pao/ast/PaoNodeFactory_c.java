package polyglot.ext.pao.ast;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.pao.extension.*;
import polyglot.util.*;
import java.util.*;

/**
 * NodeFactory for pao extension.
 */
public class PaoNodeFactory_c extends NodeFactory_c implements PaoNodeFactory {
    public Del defaultExt() {
        return new PaoDel_c();
    }

    public Instanceof Instanceof(Position pos, Expr expr, TypeNode type) {
	return new Instanceof_c(new PaoInstanceofDel_c(), pos, expr, type);
    }

    public Cast Cast(Position pos, TypeNode type, Expr expr) {
	return new Cast_c(new PaoCastDel_c(), pos, type, expr);
    }

    public Binary Binary(Position pos, Expr left, Binary.Operator op, Expr right) {
	return new Binary_c(new PaoBinaryDel_c(), pos, left, op, right);
    }
}

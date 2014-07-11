package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.ClassLit;
import polyglot.ast.Expr;
import polyglot.ast.JLang;
import polyglot.ast.NullLit;
import polyglot.ast.Term;
import polyglot.ast.TermOps;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.Traverser;

public class JL5TermExt extends JL5Ext implements TermOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Term node() {
        return (Term) super.node();
    }

    @Override
    public Term firstChild(Traverser v) {
        return ((JLang) v.superLang(lang())).firstChild(node(), v);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        return v.superLang(lang()).acceptCFG(node(), v, succs);
    }

    // TODO Is this in the right place?
    public static void checkAnnotationValueConstant(Term value, Traverser v)
            throws SemanticException {
        if (value instanceof ElementValueArrayInit) {
            // check elements
            for (Term next : ((ElementValueArrayInit) value).elements()) {
                if (!isAnnotationValueConstant(next, v)) {
                    throw new SemanticException("Annotation attribute value must be constant",
                                                next.position());
                }
            }
        }
        else if (value instanceof AnnotationElem) {
            return;
        }
        else if (!isAnnotationValueConstant(value, v)) {
            throw new SemanticException("Annotation attribute value must be constant: "
                                                + value,
                                        value.position());
        }
    }

    protected static boolean isAnnotationValueConstant(Term value, Traverser v) {
        if (value == null || value instanceof NullLit
                || value instanceof ClassLit) {
            // for purposes of annotation elems class lits are constants
            // we're ok, try the next one.
            return true;
        }
        if (value instanceof Expr) {
            Expr ev = (Expr) value;
            if (v.lang().constantValueSet(ev, v) && v.lang().isConstant(ev, v)) {
                // value is a constant
                return true;
            }
            if (ev instanceof EnumConstant) {
                // Enum constants are constants for our purposes.
                return true;
            }
            if (!v.lang().constantValueSet(ev, v)) {
                // the constant value hasn't been set yet...
                return true; // TODO: should this throw a missing dependency exception?
            }
        }
        return false;
    }
}

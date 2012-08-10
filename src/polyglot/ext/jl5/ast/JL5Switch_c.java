package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.SwitchElement;
import polyglot.ast.Switch_c;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;

public class JL5Switch_c extends Switch_c implements JL5Switch {

    public JL5Switch_c(Position pos, Expr expr, List<SwitchElement> elements) {
        super(pos, expr, elements);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {

        if (!isAcceptableSwitchType(expr.type())) {
            throw new SemanticException("Switch index must be of type char, byte, short, int, Character, Byte, Short, Integer, or an enum type.",
                                        position());
        }

        ArrayList<SwitchElement> newels =
                new ArrayList<SwitchElement>(elements.size());
        Type switchType = expr.type();
        for (SwitchElement el : elements()) {
            if (el instanceof JL5Case) {
                el =
                        (SwitchElement) ((JL5Case) el).resolveCaseLabel(tc,
                                                                        switchType);
            }
            newels.add(el);
        }
        return elements(newels);
    }

    protected boolean isAcceptableSwitchType(Type type) {
        JL5TypeSystem ts = (JL5TypeSystem) type.typeSystem();
        if (ts.Char().equals(type) || ts.Byte().equals(type)
                || ts.Short().equals(type) || ts.Int().equals(type)) {
            return true;
        }
        if (ts.wrapperClassOfPrimitive(ts.Char()).equals(type)
                || ts.wrapperClassOfPrimitive(ts.Byte()).equals(type)
                || ts.wrapperClassOfPrimitive(ts.Short()).equals(type)
                || ts.wrapperClassOfPrimitive(ts.Int()).equals(type)) {
            return true;
        }
        if (type.isClass() && JL5Flags.isEnum(type.toClass().flags())) {
            return true;
        }
        return false;
    }

}

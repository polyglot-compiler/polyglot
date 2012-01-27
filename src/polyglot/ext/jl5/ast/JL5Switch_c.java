package polyglot.ext.jl5.ast;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Switch_c;
import polyglot.ext.jl5.types.JL5Context;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;

public class JL5Switch_c extends Switch_c implements JL5Switch  {

    public JL5Switch_c(Position pos, Expr expr, List elements){
        super(pos, expr, elements);
    }

    @Override
	public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem)tc.typeSystem();
        JL5Context context = (JL5Context)tc.context();
        JL5NodeFactory nf = (JL5NodeFactory)tc.nodeFactory();

        if (!((expr.type().isClass() && JL5Flags.isEnum(expr.type().toClass().flags())) 
        		|| ts.isImplicitCastValid(expr.type(), ts.Int()))) {
            throw new SemanticException("Switch index must be an integer or enum.",
                                        position());
        }
   
        ArrayList newels = new ArrayList(elements.size());
        Type switchType = expr.type();
        for(Iterator it = elements().iterator(); it.hasNext();) {
        	Node el = (Node) it.next();
        	if(el instanceof JL5Case) {
        		el = ((JL5Case) el).resolveCaseLabel(tc, switchType);
        	}
        	newels.add(el);
        }
        return elements(newels);
    }

}

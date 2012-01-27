package polyglot.ext.jl5.ast;

import polyglot.ast.AbstractDelFactory_c;
import polyglot.ast.DelFactory;
import polyglot.ast.JL;

public class JL5DelFactory_c extends AbstractDelFactory_c implements DelFactory {

    public JL5DelFactory_c() {
        super();        
    }
    public JL5DelFactory_c(DelFactory delFactory) {
        super(delFactory);
    }
    
    @Override
    protected JL delAssignImpl() {
        return new JL5AssignDel();
    }

    @Override
    protected JL delNodeImpl() {
        return new JL5Del();
    }

}

package polyglot.ext.jl7.ast;

import polyglot.ast.JL;
import polyglot.ext.jl5.ast.JL5DelFactory_c;

public class JL7DelFactory_c extends JL5DelFactory_c implements JL7DelFactory {

    @Override
    protected JL delCaseImpl() {
        return new JL7CaseDel();
    }

}

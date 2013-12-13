package polyglot.ext.jl7.ast;

import polyglot.ast.JLDel;
import polyglot.ext.jl5.ast.JL5AbstractDelFactory_c;

public final class JL7DelFactory_c extends JL7AbstractDelFactory_c implements
JL7DelFactory {

    @Override
    protected JLDel delNodeImpl() {
        return new JL7Del();
    }

//    @Override
//    protected JLDel delCaseImpl() {
//        return new JL7CaseDel();
//    }
//
//    @Override
//    protected JLDel delSwitchImpl() {
//        return new JL7SwitchDel();
//    }
//
//    @Override
//    protected JLDel delTryImpl() {
//        return new JL7TryDel();
//    }
}

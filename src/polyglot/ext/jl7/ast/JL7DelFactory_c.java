package polyglot.ext.jl7.ast;

import polyglot.ast.JLDel;
import polyglot.ext.jl5.ast.JL5DelFactory_c;

public class JL7DelFactory_c extends JL5DelFactory_c implements JL7DelFactory {

    @Override
    protected JLDel delCaseImpl() {
        return new JL7CaseDel();
    }

    @Override
    protected JLDel delSwitchImpl() {
        return new JL7SwitchDel();
    }

    @Override
    protected JLDel delThrowImpl() {
        return new JL7ThrowDel();
    }

    @Override
    protected JLDel delTryImpl() {
        return new JL7TryDel();
    }

    @Override
    public JLDel delMultiCatch() {
        JLDel e = delMultiCatchImpl();

        if (nextDelFactory() != null) {
            JLDel e2 = nextDelFactory().delEnumDecl();
            e = composeDels(e, e2);
        }
        return postDelMultiCatch(e);
    }

    protected JLDel delMultiCatchImpl() {
        return delCatchImpl();
    }

    protected JLDel postDelMultiCatch(JLDel e) {
        return postDelCatch(e);
    }

}

package polyglot.ext.jl7.ast;

import polyglot.ast.JLDel;
import polyglot.ext.jl5.ast.JL5AbstractDelFactory_c;

public abstract class JL7AbstractDelFactory_c extends JL5AbstractDelFactory_c
        implements JL7DelFactory {

    @Override
    public JL7DelFactory nextDelFactory() {
        return (JL7DelFactory) super.nextDelFactory();
    }

    @Override
    public final JLDel delMultiCatch() {
        JLDel e = delMultiCatchImpl();

        if (nextDelFactory() != null) {
            JLDel e2 = nextDelFactory().delMultiCatch();
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

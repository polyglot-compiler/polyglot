package polyglot.ext.pao.ast;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.pao.extension.*;
import polyglot.util.*;
import java.util.*;

/**
 * NodeFactory for pao extension.
 */
public class PaoNodeFactory_c extends ExtNodeFactory_c implements PaoNodeFactory {
    public PaoNodeFactory_c() {
        super(new NodeFactory_c());
    }

    public Node extNode(Node n) {
        return n.ext(new PaoExt_c());
    }

    public Instanceof extInstanceof(Instanceof n) {
        return (Instanceof) super.extInstanceof(n).del(new PaoInstanceofDel_c()).ext(new PaoInstanceofExt_c());
    }

    public Cast extCast(Cast n) {
        return (Cast) super.extCast(n).ext(new PaoCastExt_c());
    }

    public Binary extBinary(Binary n) {
        return (Binary) super.extBinary(n).ext(new PaoBinaryExt_c());
    }
}

package polyglot.ext.pao.ast;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.pao.extension.*;
import polyglot.util.*;
import java.util.*;

/**
 * ExtFactory for pao extension.
 */
public class PaoExtFactory_c extends AbstractExtFactory_c  {
    public PaoExtFactory_c() {
        super();
    }

    public Ext extNodeImpl() {
        return new PaoExt_c();
    }

    public Ext extInstanceofImpl() {
        return new PaoInstanceofExt_c();
    }

    public Ext extCastImpl() {
        return new PaoCastExt_c();
    }

    public Ext extBinaryImpl() {
        return new PaoBinaryExt_c();
    }
}

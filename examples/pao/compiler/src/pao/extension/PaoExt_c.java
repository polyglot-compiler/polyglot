package polyglot.ext.pao.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.pao.ast.*;
import polyglot.ext.pao.types.*;
import polyglot.types.*;
import polyglot.frontend.*;
import polyglot.visit.*;
import polyglot.util.*;

public class PaoExt_c extends Ext_c implements PaoExt {
    public Node rewrite(PaoTypeSystem ts, NodeFactory nf) {
        return node();
    }
}

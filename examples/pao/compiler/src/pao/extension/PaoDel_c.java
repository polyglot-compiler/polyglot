package polyglot.ext.pao.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.pao.ast.*;
import polyglot.ext.pao.types.*;
import polyglot.types.*;
import polyglot.frontend.*;
import polyglot.visit.*;
import polyglot.util.*;

public class PaoDel_c extends Del_c implements PaoDel {
    public Node rewrite(PaoTypeSystem ts, PaoNodeFactory nf) {
        return node();
    }
}

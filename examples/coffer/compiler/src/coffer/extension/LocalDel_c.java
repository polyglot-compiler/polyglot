package polyglot.ext.coffer.extension;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.coffer.ast.*;
import polyglot.ext.coffer.types.*;

import java.util.*;

public class LocalDel_c extends CofferDel_c {
    public void checkHeldKeys(KeySet held, KeySet stored) throws SemanticException {
        Local e = (Local) node();

        if (e.type() instanceof CofferClassType) {
            Key key = ((CofferClassType) e.type()).key();

            if (key != null) {
                if (! stored.contains(key)) {
                    throw new SemanticException(
                        "Can evaluate expression of type \"" +
                        e.type() + "\" only if key \"" + key +
                        "\" is held by \"" + e.name() + "\".", e.position());

                }
            }
        }
    }
}

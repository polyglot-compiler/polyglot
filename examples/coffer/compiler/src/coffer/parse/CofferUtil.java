package polyglot.ext.coffer.parse;

import polyglot.ast.*;
import polyglot.parse.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.ext.jl.parse.JLUtil;
import polyglot.ext.coffer.ast.*;
import java.util.*;

/**
 * coffer implementation of Util.
 */
class CofferUtil extends JLUtil {

    /**
     * Construct a CofferUtil for use with the wrapped Coffer parser.
     */
    public CofferUtil(CofferParserWrapper parser) {
        super(parser);
    }

    // TODO: implement any utility methods needed by your parser.
}

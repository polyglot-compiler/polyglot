package polyglot.ext.pao.parse;

import polyglot.ast.*;
import polyglot.parse.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.ext.jl.parse.JLUtil;
import polyglot.ext.pao.ast.*;
import java.util.*;

/**
 * pao implementation of Util.
 */
class PaoUtil extends JLUtil {

    /**
     * Construct a PaoUtil for use with the wrapped Pao parser.
     */
    public PaoUtil(PaoParserWrapper parser) {
        super(parser);
    }

    // TODO: implement any utility methods needed by your parser.
}

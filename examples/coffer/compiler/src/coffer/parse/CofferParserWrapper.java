package polyglot.ext.coffer.parse;

import polyglot.ext.coffer.types.*;
import polyglot.ext.coffer.ast.*;
import polyglot.ast.*;
import polyglot.parse.*;
import polyglot.types.*;
import polyglot.util.*;

/**
 * Wrapper for a coffer parser.  This class should not need modified.
 */
public class CofferParserWrapper implements ParserWrapper {
    protected Grm parser;

    public CofferParserWrapper(Grm parser) {
        this.parser = parser;
    }

    public NodeFactory nodeFactory() {
        return parser.nf;
    }

    public TypeSystem typeSystem() {
        return parser.ts;
    }

    public void die(String message, Position pos) throws Exception {
        parser.die(message, pos);
    }

    public void die(Position pos) throws Exception {
        parser.die(pos);
    }
}


package polyglot.ext.pao.parse;

import polyglot.ext.pao.types.*;
import polyglot.ext.pao.ast.*;
import polyglot.ast.*;
import polyglot.parse.*;
import polyglot.types.*;
import polyglot.util.*;

/**
 * Wrapper for a pao parser.  This class should not need modified.
 */
public class PaoParserWrapper implements ParserWrapper {
    protected Grm parser;

    public PaoParserWrapper(Grm parser) {
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


package jltools.ext.carray.parse;

import jltools.ext.carray.types.*;
import jltools.ext.carray.ast.*;
import jltools.ast.*;
import jltools.parse.*;
import jltools.types.*;
import jltools.util.*;

/**
 * Wrapper for a carray parser. Adds a new utility method, as
 * well as implementing the standard methods of ParserWrapper.
 */
public class CarrayParserWrapper implements ParserWrapper {
        protected Grm parser;

        public CarrayParserWrapper(Grm parser) {
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

        /**
         * Return a TypeNode representing a <code>dims</code>-dimensional
         * const array of <code>n</code>.
         */
        public TypeNode constArray(TypeNode n, int dims) throws Exception {
                if (dims > 0) {
                        CarrayNodeFactory nf = (CarrayNodeFactory)nodeFactory();
                        if (n instanceof CanonicalTypeNode) {
                                Type t = ((CanonicalTypeNode) n).type();
                                CarrayTypeSystem ts = (CarrayTypeSystem)typeSystem();
                                return nf.CanonicalTypeNode(n.position(), ts.constArrayOf(n.position(), t, dims));
                        }
                        return nf.ConstArrayTypeNode(n.position(), constArray(n, dims-1));
                } else {
                        return n;
                }
        }

}


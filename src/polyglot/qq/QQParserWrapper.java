package polyglot.ext.jl.qq;

import polyglot.ast.*;
import polyglot.parse.*;
import polyglot.types.*;
import polyglot.util.*;

/**
 * Wrapper for a QQ parser.
 */
public class QQParserWrapper implements ParserWrapper {
	protected Grm parser;
	
	public QQParserWrapper(Grm parser) {
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


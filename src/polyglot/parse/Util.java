package polyglot.parse;

import polyglot.ast.*;
import polyglot.lex.*;
import polyglot.types.*;
import polyglot.util.*;

import java.util.*;

/**
 * Utility routines for use by the parser.  
 * 
 * Although some routines are static, the intended use is to instantiate this 
 * class on a ParserWrapper and use the resulting object in the parser.
 */
public class Util {
	protected ParserWrapper parser;
	protected NodeFactory nf;
	protected TypeSystem ts;
	
	/**
	 * Construct a utility object for use by <code>parser</code>.
	 */
	public Util(ParserWrapper parser) {
		this.parser = parser;
		this.nf = parser.nodeFactory();
		this.ts = parser.typeSystem();
	}

        public ParserWrapper parser() {
                return parser;
        }
	
	/**
	 * Return the position of the Token.
	 */
        public static Position pos(Token t) {
		if (t == null) {
			return null;
		}
		return t.getPosition();
	}
	
	/**
	 * Return the source position of the Type.
	 */
	public static Position pos(Type n) {
		if (n == null) {
			return null;
		}
		return n.position();
	}

	/**
	 * Return the source position of the first element in the list.
	 */
	public static Position pos(List l) {
		if (l == null || l.isEmpty()) {
			return null;
		}

		Object n = l.get(0);

		if (n instanceof Node) {
			return pos((Node) n);
		}

		if (n instanceof Token) {
			return pos((Token) n);
		}

		if (n instanceof Type) {
			return pos((Type) n);
		}

		if (n instanceof List) {
			return pos((List) n);
		}

		return null;
	}

	/**
	 * Return the source position of the declaration.
	 */
	public static Position pos(VarDeclarator n) {
		if (n == null) {
			return null;
		}
		return n.pos;
	}
	
	/**
	 * Return the source position of the Node.
	 */
	public static Position pos(Node n) {
		if (n == null) {
			return null;
		}
		return n.position();
	}

	/**
	 * Return a TypeNode representing a <code>dims</code>-dimensional
	 * array of <code>n</code>.
	 */
	public TypeNode array(TypeNode n, int dims) throws Exception {
		if (dims > 0) {
			if (n instanceof CanonicalTypeNode) {
				Type t = ((CanonicalTypeNode) n).type();
				return nf.CanonicalTypeNode(pos(n), ts.arrayOf(t, dims));
			}
			return nf.ArrayTypeNode(pos(n), array(n, dims-1));
		} else {
			return n; 
		}
	}

	/**
	 * Helper for exprToType.
	 */
	protected QualifierNode prefixToQualifier(Prefix p) throws Exception {
		if (p instanceof TypeNode) {
			return typeToQualifier((TypeNode) p);
		}

		if (p instanceof Expr) {
			return exprToQualifier((Expr) p);
		}

		if (p instanceof AmbReceiver) {
			AmbReceiver a = (AmbReceiver) p;

			if (a.prefix() != null) {
				return nf.AmbQualifierNode(pos(p),
										   prefixToQualifier(a.prefix()),
										   a.name());
			} else {
				return nf.AmbQualifierNode(pos(p), a.name());
			}
		}

		if (p instanceof AmbPrefix) {
			AmbPrefix a = (AmbPrefix) p;

			if (a.prefix() != null) {
				return nf.AmbQualifierNode(pos(p),
										   prefixToQualifier(a.prefix()),
										   a.name());
			} else {
				return nf.AmbQualifierNode(pos(p), a.name());
			}
		}

		parser.die(pos(p));
		return null;
	}

	/**
	 * Helper for exprToType.
	 */
	protected QualifierNode typeToQualifier(TypeNode t) throws Exception {
		if (t instanceof AmbTypeNode) {
			AmbTypeNode a = (AmbTypeNode) t;

			if (a.qualifier() != null) {
				return nf.AmbQualifierNode(pos(t), a.qual(), a.name());
			} else {
				return nf.AmbQualifierNode(pos(t), a.name());
			}
		}

		parser.die(pos(t));
		return null;
	}

	/**
	 * Helper for exprToType.
	 */
	protected QualifierNode exprToQualifier(Expr e) throws Exception {
		if (e instanceof AmbExpr) {
			AmbExpr a = (AmbExpr) e;
			return nf.AmbQualifierNode(pos(e), a.name());
		}

		if (e instanceof Field) {
			Field f = (Field) e;
			Receiver r = f.target();
			return nf.AmbQualifierNode(pos(e), prefixToQualifier(r), f.name());
		}

		parser.die(pos(e));
		return null;
	}

	/**
	 * Convert <code>e</code> into a type, yielding a <code>TypeNode</code>.
	 * This is used by the cast_expression production.
	 */
	public TypeNode exprToType(Expr e) throws Exception {
		if (e instanceof AmbExpr) {
			AmbExpr a = (AmbExpr) e;
			return nf.AmbTypeNode(pos(e), a.name());
		}

		if (e instanceof Field) {
			Field f = (Field) e;
			Receiver r = f.target();
			return nf.AmbTypeNode(pos(e), prefixToQualifier(r), f.name());
		}
		
		parser.die(pos(e));
		return null;
	}	
}

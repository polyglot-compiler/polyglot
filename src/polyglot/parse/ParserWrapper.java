package polyglot.parse;

import polyglot.ast.*;
import polyglot.lex.*;
import polyglot.parse.*;
import polyglot.types.*;
import polyglot.util.*;

/**
 * A wrapper for a parser to provide access to certain data and methods
 * needed by the <code>Util</code> class.  
 * 
 * It would be better just to have the CUP parser implement this interface,
 * rather than requiring separate implementations of it, but CUP doesn't allow
 * us to specify that.
 */
public interface ParserWrapper {
	
	/**
	 * Return the NodeFactory associated with the parser.
	 */
	NodeFactory nodeFactory();
	
	/**
	 * Return the TypeSystem associated with the parser.
	 */
	TypeSystem typeSystem();
	
	/**
	 * Cause the parser to abort.
	 */
	void die(String message, Position pos) throws Exception;
	
	/**
	 * Cause the parser to abort.
	 */
	void die(Position pos) throws Exception;
	
}

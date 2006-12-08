package polyglot.frontend;

import java.io.Reader;
import java.io.OutputStream;
import java.util.*;

import polyglot.ast.NodeFactory;
import polyglot.ast.NodeFactory_c;
import polyglot.parse.Grm;
import polyglot.parse.Lexer_c;
import polyglot.types.TypeSystem_c;
import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.main.Version;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

/**
 * This is the default <code>ExtensionInfo</code> for the Java language.
 *
 * Compilation passes and visitors:
 * <ol>
 * <li> parse </li>
 * <li> build-types (TypeBuilder) </li>
 * <hr>
 * <center>BARRIER</center>
 * <hr>
 * <li> disambiguate (AmbiguityRemover) </li>
 * <hr>
 * <li> type checking (TypeChecker) </li>
 * <li> reachable checking (ReachChecker) </li>
 * <li> exception checking (ExceptionChecker)
 * <li> exit checking (ExitChecker)
 * <li> initialization checking (InitChecker)
 * <li> circular constructor call checking (ConstructorCallChecker)
 * <hr>
 * <center>PRE_OUTPUT MARKER</center>
 * <hr>
 * <li> serialization (ClassSerializer), optional </li>
 * <li> translation (Translator) </li>
 * </ol>
 */
public class JLExtensionInfo extends ParserlessJLExtensionInfo {


    /**
     * Return a parser for <code>source</code> using the given
     * <code>reader</code>.
     */
    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
	polyglot.lex.Lexer lexer = new Lexer_c(reader, source, eq);
	polyglot.parse.BaseParser parser = new Grm(lexer, ts, nf, eq);

	return new CupParser(parser, source, eq);
    }
    

}

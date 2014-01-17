package polyglot.ast;

import polyglot.types.Flags;
import polyglot.util.CodeWriter;
import polyglot.visit.PrettyPrinter;

/**
 * This interface allows extension delegates both to override and reuse
 * functionality in ConstructorDecl_c and MethodDecl_c.
 *
 */
public interface ProcedureDeclOps {

    void prettyPrintHeader(Flags flags, CodeWriter w, PrettyPrinter tr);
}

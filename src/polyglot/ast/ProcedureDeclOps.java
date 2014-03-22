package polyglot.ast;

import polyglot.types.Flags;
import polyglot.util.CodeWriter;
import polyglot.visit.PrettyPrinter;

/**
 * This interface allows extension delegates both to override and reuse
 * functionality in {@code ConstructorDecl_c} and {@code MethodDecl_c}.
 *
 */
public interface ProcedureDeclOps {

    /** Pretty-print the procedure's header using the given code writer. */
    void prettyPrintHeader(Flags flags, CodeWriter w, PrettyPrinter tr);
}

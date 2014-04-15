package polyglot.ast;

import polyglot.types.ConstructorInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.visit.PrettyPrinter;

/**
 * This interface allows extension delegates both to override and reuse functionality in ClassDecl_c.
 *
 */
public interface ClassDeclOps {

    void prettyPrintHeader(CodeWriter w, PrettyPrinter tr);

    void prettyPrintFooter(CodeWriter w, PrettyPrinter tr);

    Node addDefaultConstructor(TypeSystem ts, NodeFactory nf,
            ConstructorInstance defaultConstructorInstance)
            throws SemanticException;
}

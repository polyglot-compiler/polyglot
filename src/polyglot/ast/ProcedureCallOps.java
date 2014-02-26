package polyglot.ast;

import polyglot.util.CodeWriter;
import polyglot.visit.PrettyPrinter;

public interface ProcedureCallOps {
    void printArgs(CodeWriter w, PrettyPrinter tr);
}

package polyglot.ast;

import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public interface NewOps extends ProcedureCallOps, ExprOps {

    TypeNode findQualifiedTypeNode(AmbiguityRemover ar, ClassType outer,
            TypeNode objectType) throws SemanticException;

    Expr findQualifier(AmbiguityRemover ar, ClassType ct)
            throws SemanticException;

    void typeCheckFlags(TypeChecker tc) throws SemanticException;

    void typeCheckNested(TypeChecker tc) throws SemanticException;

    void printQualifier(CodeWriter w, PrettyPrinter tr);

    void printBody(CodeWriter w, PrettyPrinter tr);

    ClassType findEnclosingClass(Context c, ClassType ct);

}

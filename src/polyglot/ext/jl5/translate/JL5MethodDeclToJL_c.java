package polyglot.ext.jl5.translate;

import polyglot.ast.ProcedureDecl;
import polyglot.ext.jl5.ast.JL5ClassDecl;
import polyglot.ext.jl5.ast.JL5MethodDecl;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.MethodDeclToExt_c;
import polyglot.translate.ext.ToExt;
import polyglot.translate.ext.ToExt_c;
import polyglot.types.SemanticException;
import polyglot.visit.NodeVisitor;

public class JL5MethodDeclToJL_c extends MethodDeclToExt_c implements ToExt {
    @Override
    public NodeVisitor toExtEnter(ExtensionRewriter rw)
            throws SemanticException {
        //Skip annotations and parameter nodes
        JL5MethodDecl cd = (JL5MethodDecl) node();
        return (ExtensionRewriter) rw.bypass(cd.typeParams());
    }
}

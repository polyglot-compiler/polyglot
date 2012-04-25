package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.Id;
import polyglot.ast.LocalDecl;
import polyglot.ast.LocalDecl_c;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.Position;

public class JL5LocalDecl_c extends LocalDecl_c implements LocalDecl {

    protected List<AnnotationElem> annotations;

    public JL5LocalDecl_c(Position pos, Flags flags, List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        super(pos, flags, type, name, init);
        this.annotations = annotations;
    }

}

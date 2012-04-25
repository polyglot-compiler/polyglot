package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.FieldDecl;
import polyglot.ast.FieldDecl_c;
import polyglot.ast.Id;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.Position;

public class JL5FieldDecl_c extends FieldDecl_c implements FieldDecl {

    protected List<AnnotationElem> annotations;

    public JL5FieldDecl_c(Position pos, Flags flags, List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        super(pos, flags, type, name, init);
        this.annotations = annotations;
    }

}

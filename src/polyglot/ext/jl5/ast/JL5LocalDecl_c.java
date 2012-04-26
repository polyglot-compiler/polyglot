package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.Id;
import polyglot.ast.LocalDecl;
import polyglot.ast.LocalDecl_c;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;

public class JL5LocalDecl_c extends LocalDecl_c implements LocalDecl {

    protected List<AnnotationElem> annotations;

    public JL5LocalDecl_c(Position pos, Flags flags, List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        super(pos, flags, type, name, init);
        this.annotations = annotations;
    }
    
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        if (child == init) {
            TypeSystem ts = av.typeSystem();

            // If the RHS is an integral constant, we can relax the expected
            // type to the type of the constant, provided that no autoboxing
            // is involved.
            if (ts.numericConversionValid(type.type(), child.constantValue())) {
                if (child.type().isPrimitive() && type.type().isPrimitive()) {
                    return child.type();
                }
            }
            return type.type();
        }

        return child.type();
    }


}

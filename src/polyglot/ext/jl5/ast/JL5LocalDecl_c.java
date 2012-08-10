package polyglot.ext.jl5.ast;

import java.util.Collections;
import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.Id;
import polyglot.ast.LocalDecl;
import polyglot.ast.LocalDecl_c;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.types.TypeVariable.TVarDecl;
import polyglot.ext.jl5.visit.AnnotationChecker;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;

public class JL5LocalDecl_c extends LocalDecl_c implements LocalDecl,
        AnnotatedElement {

    protected List<AnnotationElem> annotations;

    public JL5LocalDecl_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        super(pos, flags, type, name, init);
        if (annotations == null) {
            annotations = Collections.emptyList();
        }
        this.annotations = annotations;
    }

    @Override
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

    @Override
    public List<AnnotationElem> annotationElems() {
        return annotations;
    }

    public LocalDecl annotations(List<AnnotationElem> annotations) {
        JL5LocalDecl_c n = (JL5LocalDecl_c) copy();
        n.annotations = annotations;
        return n;
    }

    protected LocalDecl reconstruct(TypeNode type, Expr init,
            List<AnnotationElem> annotations) {
        if (this.type() != type || this.init() != init
                || !CollectionUtil.equals(annotations, this.annotations)) {
            JL5LocalDecl_c n = (JL5LocalDecl_c) copy();
            n.type = type;
            n.init = init;
            n.annotations = annotations;
            return n;
        }
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode type = (TypeNode) visitChild(this.type(), v);
        Expr init = (Expr) visitChild(this.init(), v);
        List<AnnotationElem> annots = visitList(this.annotations, v);
        return reconstruct(type, init, annots);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        if (!flags().clear(Flags.FINAL).equals(Flags.NONE)) {
            throw new SemanticException("Modifier: " + flags().clearFinal()
                    + " not allowed here.", position());
        }
        if (type().type() instanceof TypeVariable
                && tc.context().inStaticContext()) {
            if (((TypeVariable) type().type()).declaredIn()
                                              .equals(TVarDecl.CLASS_TYPE_VARIABLE))
                throw new SemanticException("Cannot access non-static type: "
                        + ((TypeVariable) type().type()).name()
                        + " in a static context.", position());
        }
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        ts.checkDuplicateAnnotations(annotations);
        return super.typeCheck(tc);
    }

    @Override
    public Node annotationCheck(AnnotationChecker annoCheck)
            throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) annoCheck.typeSystem();
        for (AnnotationElem element : annotations) {
            ts.checkAnnotationApplicability(element, this.localInstance());
        }
        return this;
    }
}

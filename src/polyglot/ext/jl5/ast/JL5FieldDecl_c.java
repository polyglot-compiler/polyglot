package polyglot.ext.jl5.ast;

import java.util.Collections;
import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.FieldDecl;
import polyglot.ast.FieldDecl_c;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5FieldInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.visit.AnnotationChecker;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5FieldDecl_c extends FieldDecl_c implements FieldDecl,
        AnnotatedElement {

    protected List<AnnotationElem> annotations;

    public JL5FieldDecl_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name, Expr init) {
        super(pos, flags, type, name, init);
        if (annotations == null) {
            annotations = Collections.emptyList();
        }
        this.annotations = annotations;
    }

    @Override
    public List<AnnotationElem> annotationElems() {
        return this.annotations;
    }

    public FieldDecl annotations(List<AnnotationElem> annotations) {
        JL5FieldDecl_c n = (JL5FieldDecl_c) copy();
        n.annotations = ListUtil.copy(annotations, true);
        return n;
    }

    protected FieldDecl_c reconstruct(TypeNode type, Expr init,
            List<AnnotationElem> annotations) {
        if (this.type() != type || this.init() != init
                || !CollectionUtil.equals(this.annotations, annotations)) {
            JL5FieldDecl_c n = (JL5FieldDecl_c) copy();
            n.type = type;
            n.init = init;
            n.annotations = ListUtil.copy(annotations, true);
            return n;
        }
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode type = (TypeNode) visitChild(this.type(), v);
        Expr init = (Expr) visitChild(this.init(), v);
        List<AnnotationElem> annotations = visitList(this.annotations, v);
        return reconstruct(type, init, annotations);
    }

    @Override
    public Node annotationCheck(AnnotationChecker annoCheck)
            throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) annoCheck.typeSystem();
        for (AnnotationElem elem : annotations) {
            ts.checkAnnotationApplicability(elem, this.fieldInstance());
        }
        return this;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5FieldDecl_c n = (JL5FieldDecl_c) super.typeCheck(tc);
        // set the retained annotations
        JL5FieldInstance fi = (JL5FieldInstance) n.fieldInstance();
        JL5TypeSystem ts = (JL5TypeSystem) fi.typeSystem();
        fi.setRetainedAnnotations(ts.createRetainedAnnotations(this.annotationElems(),
                                                               this.position()));
        return n;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        for (AnnotationElem ae : annotations) {
            ae.prettyPrint(w, tr);
            w.newline();
        }

        super.prettyPrint(w, tr);
    }
}

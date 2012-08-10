package polyglot.ext.jl5.ast;

import java.util.Collections;
import java.util.List;

import polyglot.ast.Formal;
import polyglot.ast.Formal_c;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5ArrayType;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.visit.AnnotationChecker;
import polyglot.types.ArrayType;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5Formal_c extends Formal_c implements JL5Formal {

    protected boolean isVarArg;
    protected List<AnnotationElem> annotations;

    public JL5Formal_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name) {
        this(pos, flags, annotations, type, name, false);
    }

    public JL5Formal_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name,
            boolean variable) {
        super(pos, flags, type, name);
        this.isVarArg = variable;
        if (annotations == null) {
            annotations = Collections.emptyList();
        }
        this.annotations = annotations;
    }

    @Override
    public boolean isVarArg() {
        return isVarArg;
    }

    protected Formal reconstruct(TypeNode type, List<AnnotationElem> annotations) {
        if (this.type() != type
                || !CollectionUtil.equals(annotations, this.annotations)) {
            JL5Formal_c n = (JL5Formal_c) copy();
            n.type = type;
            n.annotations = annotations;
            return n;
        }
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode type = (TypeNode) visitChild(this.type(), v);
        List<AnnotationElem> annots = visitList(this.annotations, v);
        return reconstruct(type, annots);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        if (!flags().clear(Flags.FINAL).equals(Flags.NONE)) {
            throw new SemanticException("Modifier: " + flags().clearFinal()
                    + " not allowed here.", position());
        }
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        ts.checkDuplicateAnnotations(annotations);
        return super.typeCheck(tc);

    }

    @Override
    public Node annotationCheck(AnnotationChecker annoCheck)
            throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) annoCheck.typeSystem();
        for (AnnotationElem elem : annotations) {
            ts.checkAnnotationApplicability(elem, this.localInstance());
        }
        return this;
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (isVarArg()) {
            ((JL5ArrayType) type().type()).setVarArg();
        }
        JL5Formal_c form = (JL5Formal_c) super.disambiguate(ar);

        return form;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(JL5Flags.clearVarArgs(flags).translate());
        if (isVarArg()) {
            w.write(((ArrayType) type.type()).base().toString());
            //print(type, w, tr);
            w.write(" ...");
        }
        else {
            print(type, w, tr);
        }
        w.write(" ");
        w.write(name.id());

    }

    @Override
    public List<AnnotationElem> annotationElems() {
        return this.annotations;
    }
}

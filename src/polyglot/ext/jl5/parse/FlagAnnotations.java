package polyglot.ext.jl5.parse;

import java.util.LinkedList;
import java.util.List;

import polyglot.ext.jl5.ast.AnnotationElem;
import polyglot.types.Flags;

public class FlagAnnotations {

    protected Flags classicFlags;
    protected List<AnnotationElem> annotations;

    public FlagAnnotations() {
        classicFlags = Flags.NONE;
        annotations = new LinkedList<AnnotationElem>();
    }

    public Flags flags() {
        return classicFlags;
    }

    public FlagAnnotations flags(Flags flags) {
        this.classicFlags = flags;
        return this;
    }

    public FlagAnnotations setFlag(Flags flags) {
        Flags f = this.flags().set(flags);
        return this.flags(f);
    }

    public FlagAnnotations annotations(List<AnnotationElem> annotations) {
        this.annotations = annotations;
        return this;
    }

    public List<AnnotationElem> annotations() {
        return annotations;
    }

    public FlagAnnotations addAnnotation(AnnotationElem o) {
        annotations.add(o);
        return this;
    }
}

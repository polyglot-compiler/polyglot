package polyglot.ext.jl5.types;

import polyglot.types.FieldInstance_c;
import polyglot.types.Flags;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.Position;

public class JL5FieldInstance_c extends FieldInstance_c implements
        JL5FieldInstance {
    protected RetainedAnnotations retainedAnnotations;

    public JL5FieldInstance_c(JL5TypeSystem ts, Position pos,
            ReferenceType container, Flags flags, Type type, String name) {
        super(ts, pos, container, flags, type, name);
    }

    @Override
    public RetainedAnnotations retainedAnnotations() {
        return this.retainedAnnotations;
    }

    @Override
    public void setRetainedAnnotations(RetainedAnnotations retainedAnnotations) {
        this.retainedAnnotations = retainedAnnotations;
    }

}

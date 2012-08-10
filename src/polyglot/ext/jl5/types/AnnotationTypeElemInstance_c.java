package polyglot.ext.jl5.types;

import java.util.Collections;

import polyglot.types.Flags;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.Position;

public class AnnotationTypeElemInstance_c extends JL5MethodInstance_c implements
        AnnotationTypeElemInstance {

    protected boolean hasDefault;

    public AnnotationTypeElemInstance_c(JL5TypeSystem ts, Position pos,
            ReferenceType container, Flags flags, Type type, String name,
            boolean hasDefault) {
        super(ts,
              pos,
              container,
              flags,
              type,
              name,
              Collections.<Type> emptyList(),
              Collections.<Type> emptyList(),
              Collections.<TypeVariable> emptyList());
        this.hasDefault = hasDefault;
    }

    @Override
    public Type type() {
        return this.returnType();
    }

    @Override
    public boolean hasDefault() {
        return hasDefault;
    }

}

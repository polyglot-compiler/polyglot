package polyglot.ext.jl5.types;

import polyglot.types.Type;
import polyglot.types.TypeObject_c;
import polyglot.util.Position;

public class AnnotationElementValueConstant_c extends TypeObject_c implements
        AnnotationElementValueConstant {
    private Type type;
    private Object constVal;

    public AnnotationElementValueConstant_c(JL5TypeSystem ts, Position pos,
            Type type, Object constVal) {
        super(ts, pos);
        this.type = type;
        this.constVal = constVal;
    }

    @Override
    public boolean isCanonical() {
        return this.type.isCanonical();
    }

}

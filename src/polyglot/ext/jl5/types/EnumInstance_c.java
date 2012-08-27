package polyglot.ext.jl5.types;

import polyglot.types.Flags;
import polyglot.types.ReferenceType;
import polyglot.util.Position;

@SuppressWarnings("serial")
public class EnumInstance_c extends JL5FieldInstance_c implements EnumInstance {

    long ordinal;

    public EnumInstance_c(JL5TypeSystem ts, Position pos,
            ReferenceType container, Flags f, String name, long ordinal) {
        super(ts, pos, container, f.set(Flags.STATIC)
                                   .set(Flags.PUBLIC)
                                   .set(Flags.FINAL)
                                   .set(JL5Flags.ENUM), container, name);
        this.type = container;
        this.ordinal = ordinal;
    }

    @Override
    public long ordinal() {
        return ordinal;
    }

    @Override
    public void setOrdinal(long ordinal) {
        this.ordinal = ordinal;
    }
}

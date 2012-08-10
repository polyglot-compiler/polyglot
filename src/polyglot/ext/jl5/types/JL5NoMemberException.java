package polyglot.ext.jl5.types;

import polyglot.types.NoMemberException;
import polyglot.util.Position;

public class JL5NoMemberException extends NoMemberException {

    public static final int ENUM_CONSTANT = 4;
    public static final int ANNOTATION = 5;

    public JL5NoMemberException(int kind, String s) {
        super(kind, s);
    }

    public JL5NoMemberException(int kind, String s, Position pos) {
        super(kind, s, pos);
    }

    @Override
    public String getKindStr() {
        switch (getKind()) {
        case ENUM_CONSTANT:
            return "enum constant";
        case ANNOTATION:
            return "annotation";
        default:
            return super.getKindStr();
        }
    }
}

package polyglot.ext.jl5.types;

import polyglot.types.Resolver;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.util.UniqueID;

public class CaptureConvertedWildCardType_c extends TypeVariable_c implements
        CaptureConvertedWildCardType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /**
     * Do not recurse when translating capture-converted wildcard appearing
     * recursively in its bound.
     */
    private boolean inBound = false;

    public CaptureConvertedWildCardType_c(TypeSystem ts, Position pos) {
        super(ts, pos, UniqueID.newID("capture"), null); // we'll replace this unknown type soon.
    }

    @Override
    public boolean isExtendsConstraint() {
        return !isSuperConstraint();
    }

    @Override
    public boolean isSuperConstraint() {
        return hasLowerBound();
    }

    @Override
    public String translate(Resolver c) {
        return toString(false);
    }

    @Override
    public String toString() {
        return toString(true);
    }

    private String toString(boolean printCaptureName) {
        StringBuffer sb = new StringBuffer();
        if (printCaptureName) {
            sb.append(name);
            sb.append("-of ");
        }
        sb.append('?');
        if (!inBound) {
            inBound = true;
            if (!ts.Object().equals(this.upperBound)) {
                sb.append(" extends ");
                sb.append(this.upperBound);
            }
            else if (lowerBound != null) {
                sb.append(" super ");
                sb.append(this.lowerBound);
            }
            inBound = false;
        }
        return sb.toString();
    }
}

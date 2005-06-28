package polyglot.ext.coffer.types;

import polyglot.types.*;

public interface ThrowConstraint extends TypeObject {
    public KeySet keys();
    public void setKeys(KeySet keys);
    public Type throwType();
    public void setThrowType(Type throwType);
}

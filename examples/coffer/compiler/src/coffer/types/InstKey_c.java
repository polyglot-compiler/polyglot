package coffer.types;

import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

public class InstKey_c extends Key_c implements InstKey
{
    public InstKey_c(TypeSystem ts, Position pos, String name) {
        super(ts, pos, name);
    }

    public boolean equalsImpl(TypeObject o) {
        if (o instanceof InstKey) {
            return name.equals(((InstKey) o).name());
        }
        return false;
    }
}

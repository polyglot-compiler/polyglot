package polyglot.ext.covarRet;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.types.Flags;
import polyglot.types.Package;
import polyglot.types.Type;
import polyglot.types.Qualifier;
import polyglot.util.*;
import java.util.*;

public class CovarRetNodeFactory extends NodeFactory_c {
    public ClassBody ClassBody(Position pos, List members) {
	return new CovarRetClassBody_c(pos, members);
    }
}

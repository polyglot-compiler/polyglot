package jltools.ext.covarRet;

import jltools.ast.*;
import jltools.ext.jl.ast.*;
import jltools.types.Flags;
import jltools.types.Package;
import jltools.types.Type;
import jltools.types.Qualifier;
import jltools.util.*;
import java.util.*;

public class CovarRetNodeFactory extends NodeFactory_c {
    public ClassBody ClassBody(Position pos, List members) {
	return new CovarRetClassBody_c(new Del_c(), pos, members);
    }
}

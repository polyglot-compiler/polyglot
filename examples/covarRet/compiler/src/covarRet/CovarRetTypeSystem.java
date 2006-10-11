package polyglot.ext.covarRet;

import polyglot.types.*;
import polyglot.util.*;
import java.util.*;

public class CovarRetTypeSystem extends TypeSystem_c
{
    public MethodInstance methodInstance(Position pos, ReferenceType container,
                                         Flags flags, Type returnType,
                                         String name, List formals,
                                         List throwTypes) { 

        return new CovarRetMethodInstance_c(this, pos, container,
                                            flags, returnType, name,
                                            formals, throwTypes);
    }
}

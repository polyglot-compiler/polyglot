package covarRet;

import java.util.List;

import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.types.TypeSystem_c;
import polyglot.util.Position;

public class CovarRetTypeSystem extends TypeSystem_c {
    @Override
    public MethodInstance methodInstance(Position pos, ReferenceType container,
            Flags flags, Type returnType, String name,
            List<? extends Type> formals, List<? extends Type> throwTypes) {

        return new CovarRetMethodInstance_c(this,
                                            pos,
                                            container,
                                            flags,
                                            returnType,
                                            name,
                                            formals,
                                            throwTypes);
    }
}

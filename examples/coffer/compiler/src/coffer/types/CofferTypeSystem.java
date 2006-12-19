/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.ext.param.types.*;
import polyglot.types.*;
import polyglot.util.*;
import java.util.*;

public interface CofferTypeSystem extends ParamTypeSystem {
    InstKey instKey(Position pos, String name);
    ParamKey paramKey(Position pos, String name);
    UnknownKey unknownKey(Position pos, String name);
    ThrowConstraint throwConstraint(Position pos, Type type, KeySet keys);

    KeySet emptyKeySet(Position pos);

    CofferMethodInstance cofferMethodInstance(Position pos,
        ReferenceType container, Flags flags, Type returnType,
        String name, List argTypes, KeySet entryKeys, KeySet returnKeys,
        List throwConstraints);

    CofferConstructorInstance cofferConstructorInstance(Position pos,
        ClassType container, Flags flags, List argTypes,
        KeySet entryKeys, KeySet returnKeys, List throwConstraints);
}

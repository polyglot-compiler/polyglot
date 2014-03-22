/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import java.util.List;

import polyglot.ext.param.types.ParamTypeSystem;
import polyglot.types.ClassType;
import polyglot.types.Flags;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.Position;

public interface CofferTypeSystem extends ParamTypeSystem<Key, Key> {
    InstKey instKey(Position pos, String name);

    ParamKey paramKey(Position pos, String name);

    UnknownKey unknownKey(Position pos, String name);

    ThrowConstraint throwConstraint(Position pos, Type type, KeySet keys);

    KeySet emptyKeySet(Position pos);

    CofferMethodInstance cofferMethodInstance(Position pos,
            ReferenceType container, Flags flags, Type returnType, String name,
            List<? extends Type> argTypes, KeySet entryKeys, KeySet returnKeys,
            List<ThrowConstraint> throwConstraints);

    CofferConstructorInstance cofferConstructorInstance(Position pos,
            ClassType container, Flags flags, List<? extends Type> argTypes,
            KeySet entryKeys, KeySet returnKeys,
            List<ThrowConstraint> throwConstraints);
}

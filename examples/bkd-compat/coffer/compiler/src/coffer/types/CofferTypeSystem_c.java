/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polyglot.ext.param.types.ParamTypeSystem_c;
import polyglot.ext.param.types.Subst;
import polyglot.frontend.Source;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.LazyClassInitializer;
import polyglot.types.MethodInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;

public class CofferTypeSystem_c extends ParamTypeSystem_c<Key, Key> implements
        CofferTypeSystem {
    @Override
    protected void initTypes() {
        // Do not initialize types.  This allows us to compile
        // java.lang.Object.
    }

    @Override
    public ParsedClassType createClassType(LazyClassInitializer init,
            Source fromSource) {
        if (!init.fromClassFile()) {
            return new CofferParsedClassType_c(this, init, fromSource);
        }
        else {
            return super.createClassType(init, fromSource);
        }
    }

    @Override
    public MethodInstance methodInstance(Position pos, ReferenceType container,
            Flags flags, Type returnType, String name,
            List<? extends Type> argTypes, List<? extends Type> excTypes) {

        List<ThrowConstraint> l = new LinkedList<ThrowConstraint>();

        for (Type t : excTypes) {
            l.add(throwConstraint(t.position(), t, emptyKeySet(pos)));
        }

        return cofferMethodInstance(pos,
                                    container,
                                    flags,
                                    returnType,
                                    name,
                                    argTypes,
                                    emptyKeySet(pos),
                                    emptyKeySet(pos),
                                    l);
    }

    @Override
    public CofferMethodInstance cofferMethodInstance(Position pos,
            ReferenceType container, Flags flags, Type returnType, String name,
            List<? extends Type> argTypes, KeySet entryKeys, KeySet returnKeys,
            List<ThrowConstraint> throwConstraints) {

        CofferMethodInstance mi =
                new CofferMethodInstance_c(this,
                                           pos,
                                           container,
                                           flags,
                                           returnType,
                                           name,
                                           argTypes,
                                           entryKeys,
                                           returnKeys,
                                           throwConstraints);
        return mi;
    }

    @Override
    public ConstructorInstance constructorInstance(Position pos,
            ClassType container, Flags flags, List<? extends Type> argTypes,
            List<? extends Type> excTypes) {

        List<ThrowConstraint> l = new LinkedList<ThrowConstraint>();

        for (Type t : excTypes) {
            l.add(throwConstraint(t.position(), t, emptyKeySet(pos)));
        }

        return cofferConstructorInstance(pos,
                                         container,
                                         flags,
                                         argTypes,
                                         emptyKeySet(pos),
                                         emptyKeySet(pos),
                                         l);
    }

    @Override
    public CofferConstructorInstance cofferConstructorInstance(Position pos,
            ClassType container, Flags flags, List<? extends Type> argTypes,
            KeySet entryKeys, KeySet returnKeys,
            List<ThrowConstraint> throwConstraints) {

        CofferConstructorInstance ci =
                new CofferConstructorInstance_c(this,
                                                pos,
                                                container,
                                                flags,
                                                argTypes,
                                                entryKeys,
                                                returnKeys,
                                                throwConstraints);
        return ci;
    }

    @Override
    public boolean canOverride(MethodInstance mi, MethodInstance mj) {
        return super.canOverride(mi, mj);
    }

    @Override
    public KeySet emptyKeySet(Position pos) {
        return new KeySet_c(this, pos);
    }

    @Override
    public InstKey instKey(Position pos, String name) {
        return new InstKey_c(this, pos, name);
    }

    @Override
    public UnknownKey unknownKey(Position pos, String name) {
        return new UnknownKey_c(this, pos, name);
    }

    @Override
    public ParamKey paramKey(Position pos, String name) {
        return new ParamKey_c(this, pos, name);
    }

    @Override
    public Subst<Key, Key> subst(Map<Key, ? extends Key> substMap) {
        return new CofferSubst_c(this, substMap);
    }

    @Override
    public ThrowConstraint throwConstraint(Position pos, Type type, KeySet keys) {
        return new ThrowConstraint_c(this, pos, type, keys);
    }

    @Override
    public Context createContext() {
        return new CofferContext_c(this);
    }

    @Override
    public Collection<Type> uncheckedExceptions() {
        return CollectionUtil.<Type> list(Error(), NullPointerException());
    }
}

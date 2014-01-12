/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.types;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import polyglot.frontend.Source;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.LazyClassInitializer;
import polyglot.types.MethodInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.PrimitiveType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem_c;
import polyglot.util.InternalCompilerError;

/**
 * Implementation of the PAO type system interface. Also overrides some
 * methods of <code>TypeSystem_c</code>.
 */
public class PaoTypeSystem_c extends TypeSystem_c implements PaoTypeSystem {

    /**
     * Returns a new instance of <code>PaoPrimitiveType_c</code>
     * @see PaoPrimitiveType_c
     */
    @Override
    public PrimitiveType createPrimitive(PrimitiveType.Kind kind) {
        return new PaoPrimitiveType_c(this, kind);
    }

    /**
     * Returns a new instance of <code>PaoParsedClassType_c</code>
     * @see PaoParsedClassType_c
     */
    @Override
    public ParsedClassType createClassType(LazyClassInitializer init,
            Source fromSource) {
        return new PaoParsedClassType_c(this, init, fromSource);
    }

    /**
     * The package that contains the runtime classes for boxing primitive
     * values as objects.
     */
    private static final String RUNTIME_PACKAGE = "pao.runtime";

    /**
     * @see pao.types.PaoTypeSystem#primitiveEquals()
     */
    @Override
    public MethodInstance primitiveEquals() {
        // The method instance could be cached for greater efficiency,
        // but we are not too worried about this.
        String name = RUNTIME_PACKAGE + ".Primitive";

        try {
            // use the system resolver to find the type named by name.
            Type ct = (Type) systemResolver().find(name);

            // create an argument list: two arguments of type Object.
            List<ClassType> args = new LinkedList<ClassType>();
            args.add(Object());
            args.add(Object());

            // take the first method "equals(Object, Object)" in ct.
            List<? extends MethodInstance> l =
                    ct.toClass().methods("equals", args);
            if (!l.isEmpty()) {
                return l.get(0);
            }
        }
        catch (SemanticException e) {
            throw new InternalCompilerError(e.getMessage());
        }

        throw new InternalCompilerError("Could not find equals method.");
    }

    @Override
    public MethodInstance getter(PrimitiveType t) {
        // The method instances could be cached for greater efficiency,
        // but we are not too worried about this.

        String methodName = t.toString() + "Value";

        // get the type used to represent boxed values of type t
        ReferenceType boxedType = boxedType(t);

        // take the first method with the appropriate name and an empty 
        // argument list, in the type boxedType
        List<? extends MethodInstance> l =
                boxedType.methods(methodName, Collections.<Type> emptyList());
        if (!l.isEmpty()) {
            return l.get(0);
        }

        throw new InternalCompilerError("Could not find getter for " + t);
    }

    @Override
    public ClassType boxedType(PrimitiveType t) {
        // The class types could be cached for greater efficiency,
        // but we are not too worried about this.

        String name =
                RUNTIME_PACKAGE + "."
                        + wrapperTypeString(t).substring("java.lang.".length());

        try {
            return ((Type) systemResolver().find(name)).toClass();

        }
        catch (SemanticException e) {
            throw new InternalCompilerError(e.getMessage());
        }
    }

    @Override
    public ConstructorInstance wrapper(PrimitiveType t) {
        // The constructor instances could be cached for greater efficiency,
        // but we are not too worried about this.

        ClassType ct = boxedType(t);
        for (ConstructorInstance ci : ct.constructors()) {
            if (ci.formalTypes().size() == 1) {
                Type argType = ci.formalTypes().get(0);
                if (equals(argType, t)) {
                    // found the appropriate constructor
                    return ci;
                }
            }
        }

        throw new InternalCompilerError("Could not find constructor for " + t);
    }
}

package polyglot.ext.pao.types;

import polyglot.types.*;
import polyglot.ext.jl.types.TypeSystem_c;
import polyglot.util.*;
import java.util.*;

/**
 * PAO type system.
 */
public class PaoTypeSystem_c extends TypeSystem_c implements PaoTypeSystem {
    public PrimitiveType createPrimitive(PrimitiveType.Kind kind) {
        return new PaoPrimitiveType_c(this, kind);
    }

    public ParsedClassType createClassType(LazyClassInitializer init) {
        return new PaoParsedClassType_c(this, init);
    }

    private static final String WRAPPER_PACKAGE = "polyglot.ext.pao.runtime";
    private static Map wrapper = null;
    private static Map getter = null;

    public MethodInstance primitiveEquals() {
        String name = WRAPPER_PACKAGE + ".Primitive";

        try {
            Type ct = (Type) systemResolver().find(name);

            List args = new LinkedList();
            args.add(Object());
            args.add(Object());

            for (Iterator i = ct.toClass().methods("equals", args).iterator();
                 i.hasNext(); ) {

                MethodInstance mi = (MethodInstance) i.next();
                return mi;
            }
        }
        catch (SemanticException e) {
            throw new InternalCompilerError(e.getMessage());
        }

        throw new InternalCompilerError("Could not find equals method.");
    }

    public MethodInstance getter(PrimitiveType t) {
        if (getter == null) {
            getter = new HashMap();
            getter.put(Boolean(), "booleanValue");
            getter.put(Byte(), "byteValue");
            getter.put(Char(), "charValue");
            getter.put(Short(), "shortValue");
            getter.put(Int(), "intValue");
            getter.put(Long(), "longValue");
            getter.put(Float(), "floatValue");
            getter.put(Double(), "doubleValue");
        }

        String methodName = (String) getter.get(t);
        ConstructorInstance ci = wrapper(t);

        for (Iterator i = ci.container().methods().iterator();
              i.hasNext(); ) {
            MethodInstance mi = (MethodInstance) i.next();
            if (mi.name().equals(methodName) && mi.formalTypes().isEmpty()) {
                return mi;
            }
        }

        throw new InternalCompilerError("Could not find getter for " + t);
    }

    public Type boxedType(PrimitiveType t) {
        return wrapper(t).container();
    }

    public ConstructorInstance wrapper(PrimitiveType t) {
        if (wrapper == null) {
            wrapper = new HashMap();
            wrapper.put(Boolean(), "Boolean");
            wrapper.put(Byte(), "Byte");
            wrapper.put(Char(), "Character");
            wrapper.put(Short(), "Short");
            wrapper.put(Int(), "Integer");
            wrapper.put(Long(), "Long");
            wrapper.put(Float(), "Float");
            wrapper.put(Double(), "Double");
        }

        String name = WRAPPER_PACKAGE + "." + (String) wrapper.get(t);

        try {
            Type ct = (Type) systemResolver().find(name);

            for (Iterator i = ct.toClass().constructors().iterator();
                 i.hasNext(); ) {
                ConstructorInstance ci = (ConstructorInstance) i.next();
                if (ci.formalTypes().size() == 1) {
                    Type argType = (Type) ci.formalTypes().get(0);
                    if (equals(argType, t)) {
                        return ci;
                    }
                }
            }
        }
        catch (SemanticException e) {
            throw new InternalCompilerError(e.getMessage());
        }

        throw new InternalCompilerError("Could not find constructor for " + t);
    }
}

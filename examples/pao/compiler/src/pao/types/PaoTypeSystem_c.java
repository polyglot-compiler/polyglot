package polyglot.ext.pao.types;

import polyglot.types.*;
import polyglot.ext.jl.types.TypeSystem_c;
import polyglot.frontend.Source;
import polyglot.util.*;
import java.util.*;

/**
 * PAO type system.
 */
public class PaoTypeSystem_c extends TypeSystem_c implements PaoTypeSystem {
    public PrimitiveType createPrimitive(PrimitiveType.Kind kind) {
        return new PaoPrimitiveType_c(this, kind);
    }

    public ParsedClassType createClassType(LazyClassInitializer init, 
                                           Source fromSource) {
        return new PaoParsedClassType_c(this, init, fromSource);
    }

    private static final String WRAPPER_PACKAGE = "polyglot.ext.pao.runtime";

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
        String methodName = t.toString() + "Value";
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
        String name = WRAPPER_PACKAGE + "." + wrapperTypeString(t).substring("java.lang.".length());

        try {
            ClassType ct = ((Type) systemResolver().find(name)).toClass();

            for (Iterator i = ct.constructors().iterator(); i.hasNext(); ) {
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

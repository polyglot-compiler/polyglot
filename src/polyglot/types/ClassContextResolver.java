package polyglot.types;

import polyglot.ast.*;
import polyglot.util.*;
import java.util.*;

/**
 * A <code>ClassContextResolver</code> looks up type names qualified with a class name.
 * For example, if the class is "A.B", the class context will return the class
 * for member class "A.B.C" (if it exists) when asked for "C".
 */
public class ClassContextResolver extends ClassResolver {
    TypeSystem ts;
    ClassType type;

    public ClassContextResolver(TypeSystem ts, ClassType type) {
	this.ts = ts;
	this.type = type;
    }

    public String toString() {
        return "(class-context " + type + ")";
    }

    public Type findType(String name) throws SemanticException {
        Types.report(1, "Looking for " + name + " in " + this);

        if (! StringUtil.isNameShort(name)) {
            throw new InternalCompilerError(
                "Cannot lookup qualified name " + name);
        }

        // Check if the name is for a member class.
        MemberClassType inner = ts.findMemberClass(type, name);

        if (inner != null) {
            return inner;
        }

        throw new NoClassException("Could not find type " + name +
            " in scope of " + type + ".");
    }

    public ClassType classType() {
	return type;
    }
}

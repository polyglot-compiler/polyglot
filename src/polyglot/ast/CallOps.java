package polyglot.ast;

import java.util.List;

import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.visit.TypeChecker;

/**
 * This interface allows extension delegates both to override and reuse functionality in Call_c.
 *
 */
public interface CallOps {

    /**
     * Used to find the missing static target of a static method call.
     * Should return the container of the method instance. 
     */
    Type findContainer(TypeSystem ts, MethodInstance mi);

    ReferenceType findTargetType() throws SemanticException;

    /**
     * Typecheck the Call when the target is null. This method finds
     * an appropriate target, and then type checks accordingly.
     * 
     * @param argTypes list of {@code Type}s of the arguments
     */
    Node typeCheckNullTarget(TypeChecker tc, List<Type> argTypes)
            throws SemanticException;

}

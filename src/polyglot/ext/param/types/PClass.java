package polyglot.ext.param.types;

import polyglot.types.*;
import polyglot.util.Position;  
import java.util.List;  

public interface PClass extends Importable {
    /**
     * The formal type parameters associated with this.
     */
    List formals();
    
    /**
     * The class associated with this.  Note that this should never
     * be used as a first-class type.
     */
    ClassType clazz();
    
    /**
     * Instantiate this.
     * @param pos The position of the instantiation
     * @param actuals The actual type parameters for the instantiation
     */
    ClassType instantiate(Position pos, List actuals) throws SemanticException;
    
    /**
     * Null instantiate this at the position of <code>clazz()</code>.
     */
    ClassType nullInstantiate();
    
    /**
     * Null instantiate this.  A null instantiation is an instantiation
     * where the actuals are identical to the formals.
     * @param pos The position of the instantiation
     */
    ClassType nullInstantiate(Position pos);
}

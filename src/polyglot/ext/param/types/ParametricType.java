package polyglot.ext.param.types;

import polyglot.types.*;
import polyglot.util.Position;  
import java.util.List;  

public interface ParametricType extends Type {
    List formals();
    Type instantiate(Position pos, List actuals) throws SemanticException;
    Type nullInstantiate(Position pos);
}

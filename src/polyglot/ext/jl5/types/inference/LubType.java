package polyglot.ext.jl5.types.inference;

import java.util.List;

import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.types.ReferenceType;
import polyglot.types.Type;

/**
 * Type that represents lub(U1,U2...) as defined in JLS 3rd edition section 15.12.2.7
 * @author Milan
 *
 */
public interface LubType extends JL5ClassType {
    public static final Kind LUB = new Kind("lub");

    Type calculateLub();

    List<ReferenceType> lubElements();
    //List<ReferenceType> bounds();

}

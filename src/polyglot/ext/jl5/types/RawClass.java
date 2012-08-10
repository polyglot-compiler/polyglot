package polyglot.ext.jl5.types;

/**
 * Represents a raw class. See JLS 3rd ed., 4.8.
 *
 */
public interface RawClass extends JL5ClassType {
    /**
     * The JL5ParsedClassType of which this is the raw version. This 
     * JL5ParsedClassType will have at least one type parameter.
     * 
     */
    JL5ParsedClassType base();

    /**
     * Return the JL5SubstClassType erased version of the raw class. This should be used very cautiously,
     * as the erased class type is not the same as the raw type. For example, given
     * class C<T extends D>, the raw class is |C|, which is not the same in the Polyglot type system
     * as the class C<D>, which is the erased version of the class.
     */
    JL5SubstClassType erased();
}

package polyglot.ext.jl5.types;

/**
 * Represents a raw class. See JLS 3rd ed.,
 * 
 */
public interface RawClass extends JL5ClassType {
	/**
	 * The JL5ParsedClassType of which this is the raw version. This
	 * JL5ParsedClassType will have at least one type parameter.
	 * 
	 */
	JL5ParsedClassType base();
}

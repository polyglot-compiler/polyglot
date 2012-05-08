package polyglot.ext.jl5.types;

public interface JL5SubstClassType extends JL5SubstType {
	/** The type on which substitutions are performed. */
	@Override
	JL5ParsedClassType base();

}

package polyglot.types;

/**
 * A place holder type used to serialize types that cannot be serialized.  
 */
public interface PlaceHolder extends Type {
	TypeObject resolve();
}

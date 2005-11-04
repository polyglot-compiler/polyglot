package polyglot.types;

/**
 * A place holder used to serialize type objects that cannot be serialized.  
 */
public interface NamedPlaceHolder extends PlaceHolder {
    String name();
}

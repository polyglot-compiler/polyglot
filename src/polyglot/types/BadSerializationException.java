package polyglot.types;

import polyglot.util.Position;

/**
 * Signals an error in the class resolver system. This exception is thrown
 * when a <code>ClassResolver</code> finds a class file that contains encoded 
 * Polyglot type information, but is unable to read in the serialized class 
 * information. The most likely cause of this exception is that the compiler 
 * (or compiler extension) has been modified since the class file was created, 
 * resulting in incompatible serializations. The solution is to delete the class 
 * file, and recompile it from the source.
 */
public class BadSerializationException extends SemanticException {
    private String className;
    
    private static String message(String className) {
    	return "The deserialization of the Polyglot type information for \"" + 
                className + "\" failed. The most likely cause of this " +
                "failure is that the compiler (or compiler extension) has " +
                "been modified since the class file was created, resulting " +
                "in incompatible serializations. The solution is to delete " +
                "the class file for the class \"" + className + 
                "\", and recompile it from the source.";
    }
    public BadSerializationException(String className) {
        super(message(className)); 
        this.className = className;
    }
    
    public BadSerializationException(String className, Position position) {
		super(message(className), position); 
        this.className = className;
    }
    
    public String getClassName() {
        return className;
    }
}

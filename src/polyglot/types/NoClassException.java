package polyglot.types;

import polyglot.util.Position;

/**
 * Signals an error in the class resolver system. This exception is thrown
 * when a <code>ClassResolver</code> is unable to resolve a given class name.
 */
public class NoClassException extends SemanticException {
  public NoClassException() {
  }

  public NoClassException(String s) {
    super(s); 
  }

  public NoClassException(Position position) {
    super(position);
  }

  public NoClassException(String s, Position position) {
    super(s, position);
  }
}

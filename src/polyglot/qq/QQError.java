package polyglot.ext.jl.qq;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

/**
 * Signals an error in the class resolver system. This exception is thrown
 * when a <code>ClassResolver</code> is unable to resolve a given class name.
 */
public class QQError extends InternalCompilerError {
    public QQError(String msg, Position pos) {
        super(msg, pos);
    }
}

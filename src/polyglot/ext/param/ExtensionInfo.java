package polyglot.ext.param;

/**
 * Param is an abstract extension implementing functionality for
 * parameterized types.
 */
public abstract class ExtensionInfo extends polyglot.frontend.JLExtensionInfo {
    static {
        // force Topics to load
        Topics t = new Topics();
    }
}

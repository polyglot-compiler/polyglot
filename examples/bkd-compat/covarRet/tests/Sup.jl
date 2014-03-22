import java.util.*;

/**
 * Test code for covarRet - Covariant Return extension
 */
public class Sup {
    /**
     * The return type of this method
     * will be overriden by a subtype of Set
     * by a subclass of this class.
     */
    public Set foo() {
	return null;
    }
    /**
     * Check that primitives still work
     */
    public int quux() {
        return 9;
    }
}

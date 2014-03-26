import java.util.*;

/**
 * Test code for covarRet - Covariant Return extension
 */
public class Sub extends Sup {
    /**
     * The return type of this method
     * is a subtype of the return type of Sup.foo
     */
    public HashSet foo() {
	return null;
    }

    /**
     * Check that primitives still work
     */
    public int quux() {
        return 7;
    }
}

import java.util.*;

/**
 * Test code for covarRet - Covariant Return extension
 */
public class C extends B {
    /**
     * The return type of this method
     * is a subtype of the return type of B.foo
     */
    public HashSet foo() {
	return null;
    }

}

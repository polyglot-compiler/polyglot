import java.util.*;

/**
 * Test code for covarRet - Covariant Return extension
 */
public class B extends A {
    /**
     * The return type of this method
     * is a subtype of the return type of Sup.foo
     */
    public Set foo() {
	return null;
    }
}

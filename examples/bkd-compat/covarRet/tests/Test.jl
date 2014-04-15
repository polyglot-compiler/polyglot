import java.util.*;

/**
 * Test code for covarRet - Covariant Return extension
 */
public class Test {
    public void bar() {
        // instantiate a Sup and a Sub,
        // and check that can assign
        // to variables appropriatly.
        Sup A = new Sup();
        Sub B = new Sub();

        // Set s = B.foo();
        HashSet t = B.foo();
        // Set u = A.foo();

        // check that primitives still work.
        // int a = A.quux();
        // float b = B.quux();

    }
}

/**
 * Test that const arrays can be subtyped covariantly
 */
import java.util.*;

public class TestI {
    Object const[] A;
    Set const[] B;
    Object const[] C;

    public void foo() {
       	A = new Object[] {new HashSet(), new HashSet()};
	B = (Set[])A;
	A = B;
    }

}

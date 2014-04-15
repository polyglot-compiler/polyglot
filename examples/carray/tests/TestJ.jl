/**
 * Test that non-const arrays cannot be subtyped covariantly
 */
import java.util.*;

public class TestJ {
    Object [] A;
    Set [] B;

    public void foo() {
       	A = new Object[] {new HashSet(), new HashSet()};
	B = (Set[])A;
	A = B;
    }

}

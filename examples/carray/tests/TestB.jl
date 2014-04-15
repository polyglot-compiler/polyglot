/**
 * Test that elements of a const array cannot be assigned values
 */
public class TestB {
    private int const [] A = {1, 2, 3};

    public void foo() {
	A[1] = 9;
    }
}

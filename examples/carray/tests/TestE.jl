/**
 * Test that we cannot assign a const array to a non-const array.
 */
public class TestE {
    int const[] A = {1, 2, 3};
    int[] B;


    public void foo() {
	B = A;
    }
}

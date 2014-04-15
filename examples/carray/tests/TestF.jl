/**
 * Test that we cannot assign a const array to a non-const array.
 */
public class TestF {
    int const[] A = {1, 2, 3};

    public void foo(int []input) {

    }    

    public void bar() {
	foo(A);
    }
}

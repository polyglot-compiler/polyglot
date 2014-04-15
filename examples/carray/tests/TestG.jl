/**
 * Test that we can assign a non-const array to a const array.
 */
public class TestG {
    int [] A = {1, 2, 3};

    public void foo(int const[] input) {

    }    

    public void bar() {
	foo(A);
    }
}

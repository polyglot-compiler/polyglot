/**
 * Test that can access an element of a const array for reading
 */
public class TestA {
    int const [] A = {1, 2, 3};

        public int foo() {
	  return A[1] + 9;
	}
}

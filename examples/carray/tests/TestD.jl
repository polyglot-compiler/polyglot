/**
 * Test that multidimensional arrays work
 */
public class TestD {
    int const[][] A = {new int []{1,2,3}, 
		       new int[]{4,5,6}, 
    		       new int[]{7,8,9}};

        public void foo() {
	  A = new int[][] {new int []{1,2,3}, 
		       new int[]{4,5,6}, 
    		       new int[]{7,8,9}};
	}
}

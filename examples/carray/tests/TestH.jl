/**
 * Test that const arrays can be cast away
 */
public class TestC {
    int const[] A;
    int [] B;

    public void foo() {
       	int[] C = (int[])A;
	int const[] D = B;	
    }

}

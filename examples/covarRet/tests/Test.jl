import java.util.*;

public class Test {
    public void bar() {
	Sup A = new Sup();
	Sub B = new Sub();

	Set s = B.foo();
	HashSet t = B.foo();
	Set u = A.foo();

	int a = A.quux();
        float b = B.quux();

    }
}

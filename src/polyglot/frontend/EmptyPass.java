package jltools.frontend;

/**
 * An <code>EmptyPass</code> does nothing.
 */
public class EmptyPass implements Pass
{
    public boolean run() {
	return true;
    }

    public String toString() {
        return "Empty";
    }
}

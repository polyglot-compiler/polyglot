package jltools.frontend;

/**
 * An <code>EmptyPass</code> does nothing.
 */
public class EmptyPass extends AbstractPass
{
    public EmptyPass() {
	super();
    }

    public boolean run() {
	return true;
    }

    public String toString() {
        return "Empty";
    }
}

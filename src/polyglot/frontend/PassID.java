package jltools.frontend;

/**
 * An opaque object used to identify passes.
 */
public class PassID
{
    String name;

    public PassID() {
	this("???");
    }

    public PassID(String name) {
	this.name = name;
    }

    /** Return a string representation of the pass identifier.  The string
     * should be used only for debugging purposes. */
    public String toString() {
        return name;
    }
}

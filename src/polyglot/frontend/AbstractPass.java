package polyglot.frontend;

/** The base class for most passes. */
public abstract class AbstractPass implements Pass
{
    Pass.ID id;

    public AbstractPass(Pass.ID id) {
        this.id = id;
    }

    public Pass.ID id() {
        return id;
    }

    public String name() {
        return id.toString();
    }

    public abstract boolean run();

    public String toString() {
	return getClass().getName() + ":" + id;
    }
}

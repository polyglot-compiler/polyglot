package polyglot.types;

/**
 * A LazyClassInitializer is responsible for initializing members of
 * a class after it has been created.  Members are initialized lazily
 * to correctly handle cyclic dependencies between classes.
 */
public interface LazyInitializer
{
    /**
     * Initialize the type object.
     * This must be called once when the object is added to the
     * root-level system resolver, but never before then.
     */
    public void initTypeObject();

    /** Return true if initTypeObject has been called. */
    public boolean isTypeObjectInitialized();
}

package polyglot.types;

/**
 * A LazyClassInitializer is responsible for initializing members of
 * a class after it has been created.  Members are initialized lazily
 * to correctly handle cyclic dependencies between classes.
 */
public interface LazyClassInitializer extends LazyInitializer
{
    /**
     * Return true if the class is from a class file.
     */
    public boolean fromClassFile();

    /** Set the class type we're initializing. */
    public void setClass(ParsedClassType ct);
    
    /**
     * Initialize <code>ct</code>'s superclass.
     * This method ensures the superclass of the class is initailized to a
     * canonical type, or throws a <code>MissingDependencyException</code>.
     */
    public void initSuperclass();
   
    /**
     * Initialize <code>ct</code>'s constructors.
     * This method ensures the list of constructors is populated with
     * canonical ConstructorInstances, or throws a <code>MissingDependencyException</code>.
     */
    public void canonicalConstructors();

    /**
     * Initialize <code>ct</code>'s methods.
     * This method ensures the list of methods is populated with
     * canonical MethodInstances, or throws a <code>MissingDependencyException</code>.
     */
    public void canonicalMethods();

    /**
     * Initialize <code>ct</code>'s fields.
     * This method ensures the list of fields is populated with
     * canonical FieldInstances, or throws a <code>MissingDependencyException</code>.
     */
    public void canonicalFields();

    /**
     * Initialize <code>ct</code>'s constructors.
     * This method ensures the list of fields is populated with (possibly
     * non-canonical) ConstructorInstances, or throws a <code>MissingDependencyException</code>.
     */
    public void initConstructors();

    /**
     * Initialize <code>ct</code>'s methods.
     * This method ensures the list of fields is populated with (possibly
     * non-canonical) MethodInstances, or throws a <code>MissingDependencyException</code>.
     */
    public void initMethods();

    /**
     * Initialize <code>ct</code>'s fields.
     * This method ensures the list of fields is populated with (possibly
     * non-canonical) FieldInstances, or throws a <code>MissingDependencyException</code>.
     */
    public void initFields();

    /**
     * Initialize <code>ct</code>'s member classes.
     * This method ensures the member classes of the class are initailized to
     * canonical types, or throws a <code>MissingDependencyException</code>.
     */
    public void initMemberClasses();

    /**
     * Initialize <code>ct</code>'s interfaces.
     * This method ensures the interfaces of the class are initailized to
     * canonical types, or throws a <code>MissingDependencyException</code>.
     */
    public void initInterfaces();
}

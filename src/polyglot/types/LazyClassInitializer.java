package polyglot.types;

/**
 * A LazyClassInitializer is responsible for initializing members of
 * a class after it has been created.  Members are initialized lazily
 * to correctly handle cyclic dependencies between classes.
 */
public interface LazyClassInitializer
{
    /**
     * Return true if the class is from a class file.
     */
    public boolean fromClassFile();

    /** Set the class type we're initializing. */
    public void setClass(ParsedClassType ct);
    
    /**
     * Initialize <code>ct</code>'s superclass.
     */
    public void initSuperclass();
   
    /**
     * Initialize <code>ct</code>'s constructors.
     */
    public void initConstructors();

    /**
     * Initialize <code>ct</code>'s methods.
     */
    public void initMethods();

    /**
     * Initialize <code>ct</code>'s fields.
     */
    public void initFields();

    /**
     * Initialize <code>ct</code>'s member classes.
     */
    public void initMemberClasses();

    /**
     * Initialize <code>ct</code>'s interfaces.
     */
    public void initInterfaces();

    public boolean constructorsInitialized();

    public boolean fieldsInitialized();

    public boolean interfacesInitialized();

    public boolean memberClassesInitialized();

    public boolean methodsInitialized();

    public boolean superclassInitialized();

    public boolean initialized();
}

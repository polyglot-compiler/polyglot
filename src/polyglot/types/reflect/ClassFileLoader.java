package polyglot.types.reflect;

import javax.tools.JavaFileManager.Location;

/**
 * Interface for defining class file loader
 */
public interface ClassFileLoader {
    /**
     * @param name
     *            A package name
     * @return true if a package <code>name</code> exists, otherwise false
     */
    boolean packageExists(String name);

    /**
     * @param location
     *            A location to search a package <code>name</code> in
     * @param name
     *            A package name
     * @return true if a package <code>name</code> exists, otherwise false
     */
    boolean packageExists(Location location, String name);

    /**
     * @param name A class name
     * @return A ClassFile if <code>name</code>.class file exists, otherwise 
     * null 
     */
    ClassFile loadFile(String name);

    /**
     * @param location A location to load a ClassFile from
     * @param name A class name
     * @return A ClassFile if <code>name</code>.class file exists in the 
     * location, otherwise null
     */
    ClassFile loadFile(Location location, String name);

    /**
     * @param loc A location to add to the ClassFileLoader
     */
    void addLocation(Location loc);
}

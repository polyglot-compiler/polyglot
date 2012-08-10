package polyglot.frontend;

import java.io.IOException;

import javax.tools.JavaFileManager.Location;

/**
 * Interface for defining source file loader
 */
public interface SourceLoader {

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
     * @param fileName
     *            A source file name
     * @return A FileSource if a source file <code>fileName</code> exists in
     *         source path
     * @throws IOException
     */
    FileSource fileSource(String fileName) throws IOException;

    /**
     * @param fileName
     *            A source file name
     * @param userSpecified
     *            If the user has specified this source file explicitly
     * @return A FileSource if a source file <code>fileName</code> exists in
     *         source path
     * @throws IOException
     */
    FileSource fileSource(String fileName, boolean userSpecified)
            throws IOException;

    /**
     * @param location
     *            A location to search a source file in
     * @param fileName
     *            A source file name
     * @return A FileSource if a source file <code>fileName</code> exists
     * @throws IOException
     */
    FileSource fileSource(Location location, String fileName)
            throws IOException;

    /**
     * @param location
     *            A location to search a source file in
     * @param fileName
     *            A source file name
     * @param userSpecified
     *            If the user has specified this source file explicitly
     * @return A FileSource if a source file <code>fileName</code> exists
     * @throws IOException
     */
    FileSource fileSource(Location location, String fileName,
            boolean userSpecified) throws IOException;

    /**
     * Load the source file for the given (possibly nested) class name using the
     * source path.
     */
    /**
     * @param className
     *            A class name
     * @return A FileSource containing definition of the class
     *         <code>className</code> if it exists in source path, otherwise
     *         null
     */
    FileSource classSource(String className);

    /**
     * @param location
     *            A location to search a file in, that contains definition of
     *            the class <code>className</code>
     * @param className
     *            A class name
     * @return A FileSource containing definition of the class
     *         <code>className</code> if it exists in <code>location</code>,
     *         otherwise null
     */
    FileSource classSource(Location location, String className);

    /**
     * @return true if the file system is case-insensitive, otherwise false.
     */
    boolean caseInsensitive();
}

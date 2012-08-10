package polyglot.types.reflect;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Interface for defining .class files
 */
public interface ClassFile {

    URI getClassFileURI();

    /**
     * Get the encoded source modified time.
     */
    long sourceLastModified(String ts);

    /**
     * Get the encoded compiler version used to compile the source.
     */
    String compilerVersion(String ts);

    /**
     * Get the encoded class type for the given type system.
     */
    String encodedClassType(String typeSystemKey);

    /**
     * Get the class name at the given constant pool index.
     */
    String classNameCP(int index);

    /**
     * Get the name of the class, including the package name.
     * 
     * @return The name of the class.
     */
    String name();

    /**
     * Read the class's attributes. Since none of the attributes are required,
     * just read the length of each attribute and skip that many bytes.
     * 
     * @param in
     *            The stream from which to read.
     * @exception IOException
     *                If an error occurs while reading.
     */
    void readAttributes(DataInputStream in) throws IOException;

    Method createMethod(DataInputStream in) throws IOException;

    Field createField(DataInputStream in) throws IOException;

    Attribute createAttribute(DataInputStream in, String name, int nameIndex,
            int length) throws IOException;

    Attribute[] getAttrs();

    Constant[] getConstants();

    Field[] getFields();

    InnerClasses getInnerClasses();

    int[] getInterfaces();

    Method[] getMethods();

    int getModifiers();

    int getSuperClass();

    int getThisClass();

}

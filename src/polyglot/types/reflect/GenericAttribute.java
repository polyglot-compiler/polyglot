package jltools.types.reflect;

import java.util.*;
import java.io.*;

/**
 * The Java Virtual Machine Specification allows implementors to 
 * invent their own attributes.  GenericAttribute models attributes 
 * whose name BLOAT does not recognize.
 *
 * @author Nate Nystrom
 *         (<a href="mailto:nystrom@cs.purdue.edu">nystrom@cs.purdue.edu</a>)
 */
class GenericAttribute extends Attribute
{
  byte[] data;

  /**
   * Constructor.  Create an attribute from a data stream.
   *
   * @param in
   *        The data stream of the class file.
   * @param nameIndex
   *        The index into the constant pool of the name of the attribute.
   * @param length
   *        The length of the attribute, excluding the header.
   * @exception IOException
   *        If an error occurs while reading.
   */
  GenericAttribute(DataInputStream in, int nameIndex, int length)
    throws IOException
  {
    super(nameIndex, length);
    data = new byte[length];
    for (int read = 0; read < length;) {
      read += in.read(data, read, length - read);
    }
  }
}

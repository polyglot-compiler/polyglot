/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * Copyright (c) 1997-2001 Purdue Research Foundation, Purdue University
 * 
 */

package polyglot.types.reflect;

import java.util.*;
import java.io.*;

/**
 * Exceptions describes the types of exceptions that a method may throw.
 * The Exceptions attribute stores a list of indices into the constant
 * pool of the typs of exceptions thrown by the method.
 *
 * @see polyglot.types.reflect Method
 *
 * @author Nate Nystrom
 *         (<a href="mailto:nystrom@cs.purdue.edu">nystrom@cs.purdue.edu</a>)
 */
class InnerClasses extends Attribute {
  private Info[] classes;

  public static class Info {
    public int classIndex;
    public int outerClassIndex;
    public int nameIndex;
    public int modifiers;
  }

  /**
   * Constructor.  Create an Exceptions attribute from a data stream.
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
  public InnerClasses(DataInputStream in, int nameIndex, int length) throws IOException
  {
    super(nameIndex, length);

    int count = in.readUnsignedShort();

    classes = new Info[count];

    for (int i = 0; i < count; i++) {
      classes[i] = new Info();

      // index of a Constant.CLASS
      classes[i].classIndex = in.readUnsignedShort();

      // index of a Constant.CLASS != 0 iff a member class.
      classes[i].outerClassIndex = in.readUnsignedShort();

      // index of a Constant.UTF == 0 iff an anonymous class.
      classes[i].nameIndex = in.readUnsignedShort();

      // modifiers of inner class
      classes[i].modifiers = in.readUnsignedShort();
    }
  }
  
  public Info[] getClasses() {
      return classes;
  }
}

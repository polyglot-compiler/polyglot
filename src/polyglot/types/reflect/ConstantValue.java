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
 * The ConstantValue attribute stores an index into the constant pool
 * that represents constant value.  A class's static fields have 
 * constant value attributes.
 *
 * @see polyglot.types.reflect Field
 *
 * @author Nate Nystrom
 *         (<a href="mailto:nystrom@cs.purdue.edu">nystrom@cs.purdue.edu</a>)
 */
public class ConstantValue extends Attribute
{
  private int index;

  /**
   * Constructor.  Create a ConstantValue attribute from a data stream.
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
  ConstantValue(DataInputStream in, int nameIndex, int length)
    throws IOException
  {
    super(nameIndex, length);
    index = in.readUnsignedShort();
  }
  
  public int getIndex() {
      return index;
  }
}

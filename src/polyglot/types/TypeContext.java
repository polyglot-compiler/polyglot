/*
 * TypeContext.java
 */

package jltools.types;

import java.util.Iterator;
import java.util.List;
import jltools.ast.*;

/**
 * TypeContext
 *
 * Overview:
 *    A TypeContext is responsible for looking up types be name.
 **/
public interface TypeContext {
  public Type getType(String name) throws SemanticException;
}

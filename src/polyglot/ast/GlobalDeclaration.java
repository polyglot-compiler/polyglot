package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

import java.util.*;
import java.io.IOException;


/**
 * A <code>GlobalDeclaration</code> is a top-level declaration with a name
 * and access flags.
 */
public interface GlobalDeclaration
{
  public String getName();
  public AccessFlags getAccessFlags();
}

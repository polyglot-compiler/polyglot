/*
 * EmptyContext.java
 */

package jltools.types;

import java.util.Iterator;
import java.util.List;
import jltools.ast.*;
import jltools.util.*;

/**
 * EmptyContext
 *
 * Overview:
 *    An EmptyContext is responsible for looking up types by name in an
 *    empty context.
 */
public class EmptyContext implements TypeContext {
  ClassResolver resolver;
  TypeSystem ts;

  EmptyContext(TypeSystem ts, ClassResolver resolver) {
    this.ts = ts;
    this.resolver = resolver;
  }

  public Type getType(String name) throws SemanticException {
    if (! TypeSystem.isNameShort(name)) {
	throw new InternalCompilerError("cannot lookup qualified name " + name);
    }

    try {
      return resolver.findClass(name);
    }
    catch (SemanticException e) {
      return new PackageType(ts, null, name);
    }
  }
}

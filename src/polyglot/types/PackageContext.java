/*
 * PackageContext.java
 */

package jltools.types;

import java.util.Iterator;
import java.util.List;
import jltools.ast.*;
import jltools.util.*;

/**
 * PackageContext
 *
 * Overview:
 *    A PackageContext is responsible for looking up types in a packge by name.
 **/
public class PackageContext implements TypeContext {
  ClassResolver resolver;
  PackageType type;

  PackageContext(ClassResolver resolver, PackageType type)
	throws SemanticException {

    this.resolver = resolver;
    this.type = type;
  }

  public Type getType(String name) throws SemanticException {
    if (! TypeSystem.isNameShort(name)) {
	throw new InternalCompilerError(
	    "getTypeByName: cannot lookup qualified name " + name);
    }

    try {
	return resolver.findClass(type.getTypeString() + "." + name);
    }
    catch (SemanticException e) {
	return new PackageType(type.getTypeSystem(), type, name);
    }
  }
}

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

    try {
	resolver.findPackage(type.getTypeString());
    }
    catch (NoClassException e) {
      throw new SemanticException("Package " + type.getTypeString() +
				  " not found");
    }
  }

  public Type getType(String name) throws SemanticException {
    if (! TypeSystem.isNameShort(name)) {
	throw new InternalCompilerError(
	    "getTypeByName: cannot lookup qualified name");
    }

    try {
	return resolver.findClass(type.getTypeString() + "." + name);
    }
    catch (SemanticException e) {
	try {
	    resolver.findPackage(type.getTypeString() + "." + name);
	    return new PackageType(type.getTypeSystem(), type, name);
	}
	catch (NoClassException e2) {
	  throw new SemanticException("Package " + type.getTypeString() +
				      "." + name + " not found");
	}
    }
  }
}

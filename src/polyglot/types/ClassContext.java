/*
 * ClassContext.java
 */

package jltools.types;

import java.util.Iterator;
import java.util.List;
import jltools.ast.*;
import jltools.util.*;

/**
 * ClassContext
 *
 * Overview:
 *    A ClassContext is responsible for looking up types in a packge by name.
 **/
public class ClassContext implements TypeContext {
  ClassResolver resolver;
  ClassType type;
  TypeSystem ts;

  ClassContext(ClassResolver resolver, ClassType type)
	throws SemanticException {

    this.resolver = resolver;
    this.type = type;
    this.ts = type.getTypeSystem();

    try {
	resolver.findClass(type.getTypeString());
    }
    catch (SemanticException e) {
      throw new SemanticException("Class " + type.getTypeString() +
				  " not found");
    }
  }

  public Type getType(String name) throws SemanticException {
    if (! TypeSystem.isNameShort(name)) {
	throw new InternalCompilerError("cannot lookup qualified name");
    }

    if (name.equals(type.getShortName())) {
	return type;
    }

    Type inner = type.getInnerNamed(name);

    if (inner != null) {
	return inner;
    }

    Type t1 = null;
    Type t2 = null;

    if (type.getSuperType() != null && type.getSuperType().isClassType()) {
	try {
	    t1 = ts.getClassContext(resolver,
		    type.getSuperType().toClassType()).getType(name);
	}
	catch (SemanticException e) {
	}
    }

    if (type.getContainingClass() != null) {
	try {
	    t2 = ts.getClassContext(resolver,
		    type.getContainingClass()).getType(name);
	}
	catch (SemanticException e) {
	}
    }

    if (t1 != null && t2 != null) {
	throw new SemanticException("Duplcate class " + name +
	    " found in scope of " + type.getFullName());
    }
    else if (t1 != null) {
	return t1;
    }
    else if (t2 != null) {
	return t2;
    }

    throw new SemanticException("Class " + name + " not found in scope of " +
		type.getFullName());
  }

  public ClassType getClassType() {
    return type;
  }
}

package jltools.frontend;

import jltools.parse.*;
import jltools.lex.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.main.Main;

import java.io.*;
import java.util.*;

/**
 * A <code>ClassTypeJob</code> encapsulates work done by the compiler on
 * behalf of a source file target.
 */
public class ClassTypeJob extends Job
{
  ClassType type;
  ErrorQueue eq;

  // New job for a decoded class type -- need to do just a clean.
  public ClassTypeJob( Target t, ClassType type,
			ErrorQueue eq, ClassResolver systemResolver )
  {
    super(t);
    this.type = type;
    this.eq = eq;

    cr.addClass(type.getFullName(), type);

    it = new ImportTable(systemResolver, true, eq);

    status = PARSED | READ | DISAMBIGUATED | CHECKED | TRANSLATED;
  }

  // Do nothing
  public void parse() {
    status |= PARSED;
  }

  // Do nothing
  public void read() {
    status |= READ;
  }

  // Do the work once done by TableClassResolver.cleanupClassSignatures()
  // and now done with ClassNode.cleanupSignatures().  This is so similar
  // to ClassNode.cleanupSignatures() that, ideally, we should use the same
  // code, but there are a bunch of minor differences.
  public void clean() {
    TypeSystem ts = type.getTypeSystem();

    // Create an empty context.  Any name that is not fully qualified
    // should result in an error.
    TypeContext context = ts.getEmptyContext(it);

    Type superType = type.getSuperType();

    if (superType != null) {
      ClassType superClazz;

      if (superType instanceof AmbiguousType &&
	  ! (type instanceof ParsedClassType)) {
	throw new InternalCompilerError(type.getTypeString() + " is a " +
	    type.getClass().getName() + " with an ambiguous super type.");
      }

      try {
	superClazz = (ClassType) ts.checkAndResolveType(superType, context);
      }
      catch (SemanticException e) {
	eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage() );
	return;
      }

      if (superClazz instanceof ParsedClassType) {
	try {
	  if (! Compiler.getCompiler().cleanClass(superClazz)) {
	    eq.enqueue( ErrorInfo.SEMANTIC_ERROR, "Errors while compiling " +
	      "superclass " + superClazz.getTypeString() + " of " +
	      type.getTypeString() + "." );
	  }
	}
	catch (IOException e) {
	  eq.enqueue( ErrorInfo.IO_ERROR, "I/O error while compiling " +
	    "superclass " + superClazz.getTypeString() + " of " +
	    type.getTypeString() + "." );
	}
      }

      if (type instanceof ParsedClassType) {
	((ParsedClassType) type).setSuperType(superClazz);
      }
    }
    else {
      if (! (type instanceof ParsedClassType)) {
	throw new InternalCompilerError(type.getTypeString() + " is a " +
	    type.getClass().getName() + " with a null super type.");
      }
      ((ParsedClassType) type).setSuperType((ClassType) ts.getObject());
    }

    for (ListIterator i = type.getInterfaces().listIterator(); i.hasNext(); ) {
      Type interfaceType = (Type) i.next();
      ClassType interfaceClazz;

      if (interfaceType instanceof AmbiguousType &&
	  ! (type instanceof ParsedClassType)) {
	throw new InternalCompilerError(type.getTypeString() + " is a " +
	    type.getClass().getName() + " with an ambiguous interface type.");
      }

      try {
	interfaceClazz = (ClassType)
	  ts.checkAndResolveType(interfaceType, context);
      }
      catch (SemanticException e) {
	eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage() );
	return;
      }

      if (interfaceClazz instanceof ParsedClassType) {
	try {
	  if (! Compiler.getCompiler().cleanClass(interfaceClazz)) {
	    eq.enqueue( ErrorInfo.SEMANTIC_ERROR, "Errors while compiling " +
	      "super-interface " + interfaceClazz.getTypeString() + " of " +
	      type.getTypeString() + "." );
	    return;
	  }
	}
	catch (IOException e) {
	  eq.enqueue( ErrorInfo.IO_ERROR, "I/O error while compiling " +
	    "super-interface " + interfaceClazz.getTypeString() + " of " +
	    type.getTypeString() + "." );
	  return;
	}
      }

      i.set(interfaceClazz);
    }

    for (ListIterator i = type.getMethods().listIterator(); i.hasNext(); ) {
      MethodTypeInstance mti = (MethodTypeInstance) i.next();

      Type rt = mti.getReturnType();

      if (rt instanceof AmbiguousType && ! (type instanceof ParsedClassType)) {
	throw new InternalCompilerError(type.getTypeString() + " is a " +
	    type.getClass().getName() + " with an ambiguous signature.");
      }

      try {
	mti.setReturnType( ts.checkAndResolveType(rt, context) );
      }
      catch (SemanticException e) {
	eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(),
	    Annotate.getLineNumber(mti) );
	return;
      }

      List argTypes = mti.argumentTypes();

      for (ListIterator j = argTypes.listIterator(); j.hasNext(); ) {
	Type t = (Type) j.next();

	if (t instanceof AmbiguousType && ! (type instanceof ParsedClassType)) {
	  throw new InternalCompilerError(type.getTypeString() + " is a " +
	      type.getClass().getName() + " with an ambiguous signature.");
	}

	try {
          j.set( ts.checkAndResolveType(t, context) );
	}
	catch (SemanticException e) {
	  eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(),
	      Annotate.getLineNumber(mti) );
	  return;
	}
      }

      List excTypes = mti.exceptionTypes();

      for (ListIterator j = excTypes.listIterator(); j.hasNext(); ) {
	Type t = (Type) j.next();

	if (t instanceof AmbiguousType && ! (type instanceof ParsedClassType)) {
	  throw new InternalCompilerError(type.getTypeString() + " is a " +
	      type.getClass().getName() + " with an ambiguous signature.");
	}

	try {
	  j.set( ts.checkAndResolveType(t, context) );
	}
	catch (SemanticException e) {
	  eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(),
	      Annotate.getLineNumber(mti) );
	  return;
	}
      }
    }

    for (ListIterator i = type.getFields().listIterator(); i.hasNext(); ) {
      FieldInstance fi = (FieldInstance) i.next();

      Type t = fi.getType();

      if (t instanceof AmbiguousType && ! (type instanceof ParsedClassType)) {
	throw new InternalCompilerError(type.getTypeString() + " is a " +
	    type.getClass().getName() + " with an ambiguous signature.");
      }

      try {
	fi.setType( ts.checkAndResolveType(t, context) );
      }
      catch (SemanticException e) {
	eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(),
	    Annotate.getLineNumber(fi) );
	return;
      }
    }

    status |= CLEANED;
  }

  // Do nothing
  public void disambiguate() {
    status |= DISAMBIGUATED;
  }
  // Do nothing
  public void check() {
    status |= CHECKED;
  }
  // Do nothing
  public void translate() {
    status |= TRANSLATED;
  }

  public void dump(CodeWriter w) throws IOException {
    w.flush();
  }
}

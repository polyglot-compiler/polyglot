package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

import java.util.*;
import java.io.IOException;


/**
 * A <code>ClassNode</code> is the definition of a class, abstract class,
 * or interface. It may be a public or other top-level class, or an inner
 * named class, or an anonymous class.
 */
public class ClassNode extends ClassMember implements GlobalDeclaration
{
  /** Defines visibililty etc. for this class. */
  protected final AccessFlags accessFlags;
  /** The name of this class. FIXME is this the short name? */
  protected final String name;
  /** The super class of this class, if any. */
  protected final TypeNode superClass;
  /** A list of interfaces that this class implements. */
  protected final List interfaces;
  /** A list of member (e.g. fields, methods) of this class. */
  protected final List members;
  /** A flag indicating the class is local. */
  protected final boolean isLocal;
  /** A flag indicating the class is anonymous. */
  protected final boolean isAnonymous;

  /**
   * FIXME: This field doesn't actually follow the immutablility rule.  It is changed
   * within the various passes, but does not result in the creation of a new 
   * ClassNode. Consequently, whenever we create a new node via reconstruct, we always
   * copy its old value to the new value.
   */
  public ParsedClassType type;
  
  /**
   * FIXME
   */
  public ClassNode(Node ext, 
		   AccessFlags accessFlags,
		   String name,
		   TypeNode superClass,
		   List interfaces,
		   List members,
		   boolean isLocal,
		   boolean isAnonymous) {

    this.ext = ext;
    this.accessFlags = accessFlags.copy();
    this.name = name;
    this.superClass = superClass;
    this.interfaces = TypedList.copyAndCheck( interfaces, 
                                              TypeNode.class, true);
    this.members = TypedList.copyAndCheck( members, 
                                           ClassMember.class, true);
    this.isLocal = isLocal;
    this.isAnonymous = isAnonymous;
  }

  public ClassNode(Node ext, 
		   AccessFlags accessFlags,
		   String name,
		   TypeNode superClass,
		   List interfaces,
		   List members) {
    this(ext, accessFlags, name, superClass, interfaces, members, false, false);
  }

  public ClassNode(AccessFlags accessFlags,
		   String name,
		   TypeNode superClass,
		   List interfaces,
		   List members) {
    this(null, accessFlags, name, superClass, interfaces, members, false, false);
  }
  
  public ClassNode(AccessFlags accessFlags,
		   String name,
		   TypeNode superClass,
		   List interfaces,
		   List members,
		   boolean isLocal,
		   boolean isAnonymous) {
    this(null, accessFlags, name, superClass, interfaces, members, isLocal, isAnonymous);
  }
  
  /**
   * Lazily reconstruct this node.
   * <p>
   * If the arguments are pointer identical the fields of the current node,
   * then the current node is returned untouched. Otherwise a new node is
   * constructed with the new fields and all annotations from this node are
   * copied over.
   */
  public ClassNode reconstruct( Node ext,
			        AccessFlags accessFlags,
                                String name,
                                TypeNode superClass,
                                List interfaces,
                                List members,
				boolean isLocal,
				boolean isAnonymous)
  {
    if( !this.accessFlags.equals( accessFlags) || !this.name.equals( name)
          || this.superClass != superClass
	  || this.ext != ext
          || this.interfaces.size() != interfaces.size()
          || this.members.size() != members.size()
          || this.isLocal != isLocal
          || this.isAnonymous != isAnonymous) {
      ClassNode n = new ClassNode( ext, accessFlags, name, superClass, 
                                   interfaces, members, isLocal, isAnonymous);

      n.type = type;
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < interfaces.size(); i++) {
        if( this.interfaces.get( i) != interfaces.get( i)) {
          ClassNode n = new ClassNode( ext, accessFlags, name, superClass, 
                                       interfaces, members, isLocal, isAnonymous);
          n.type = type;
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      for( int i = 0; i < members.size(); i++) {
        if( this.members.get( i) != members.get( i)) {
          ClassNode n = new ClassNode( ext, accessFlags, name, superClass, 
                                       interfaces, members, isLocal, isAnonymous);
          n.type = type;
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      return this;
    }
  }

  public ClassNode reconstruct( Node ext,
			        AccessFlags accessFlags,
                                String name,
                                TypeNode superClass,
                                List interfaces,
                                List members) {
      return reconstruct(ext, accessFlags, name, superClass, interfaces, members, false, false);
  }

  public ClassNode reconstruct( AccessFlags accessFlags,
                                String name,
                                TypeNode superClass,
                                List interfaces,
                                List members,
				boolean isLocal,
				boolean isAnonymous) {
    return reconstruct(this.ext, accessFlags, name, superClass, interfaces, members, isLocal, isAnonymous);
  }

  public ClassNode reconstruct( AccessFlags accessFlags,
                                String name,
                                TypeNode superClass,
                                List interfaces,
                                List members) {
      return reconstruct(this.ext, accessFlags, name, superClass, interfaces, members, false, false);
  }

  /**
   * Returns the <code>AccessFlags</code> for this class declaration. 
   */
  public AccessFlags getAccessFlags() {
    return accessFlags;
  }
  
  /** 
   * Returns the name of this class.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the type of the super class of the the class defined here.
   */
  public TypeNode getSuperClass() 
  {
    return superClass;
  }

  /**
   * Returns the interface at position <pos>. 
   */
  public TypeNode getInterfaceAt( int pos) 
  {
    return (TypeNode)interfaces.get( pos);
  }

  /**
   * Returns a <code>Iterator</code> which will return the 
   * <code>TypeNode</code>s of the interfaces implemented by this 
   * <code>ClassNode</code> in order.
   */
  public Iterator interfaces() 
  {
    return interfaces.iterator();
  }

  /**
   * Returns the member of the class body at <code>pos</code>.
   */
  public ClassMember getMemberAt( int pos) 
  {
    return (ClassMember)members.get( pos);
  }

  /**
   * Returns a <code>Iterator</code> which yields each class member of this
   *  class in order.  
   */
  public Iterator members() 
  {
    return members.iterator();
  }

  /**
   * Visit the children of this node.
   *
   * @pre Requires that <code>superClass.visit</code> returns an object of
   *  type <code>TypeNode</code> and that the <code>visit</code> method of 
   *  each interface and member returns an object of types 
   *  <code>TypeNode</code> and <code>ClassMember</code> respectively.
   * @post Returns <code>this</code> if no changes are made, otherwise a copy
   *  is made and returned.   
   */
  public Node visitChildren( NodeVisitor v) 
  {
    TypeNode newSuperClass = null;

    if( superClass != null) {
      newSuperClass = (TypeNode)superClass.visit( v);
    }

    List newInterfaces = new ArrayList( interfaces.size()),
      newMembers = new ArrayList( members.size());

    for( Iterator iter = interfaces(); iter.hasNext(); ) {
      TypeNode tn = (TypeNode)((TypeNode)iter.next()).visit( v);
      if( tn != null) {
        newInterfaces.add( tn);
      }
    }

    for( Iterator iter = members(); iter.hasNext(); ) {
      ClassMember member = (ClassMember)((ClassMember)iter.next()).visit( v);
      if( member != null) {
        newMembers.add( member);
      }
    }

    return reconstruct( Node.condVisit(this.ext, v), 
			accessFlags,
                        name,
                        newSuperClass,
                        newInterfaces,
                        newMembers,
			isLocal,
			isAnonymous);
  }

  public Node readSymbols( SymbolReader sr) throws SemanticException
  {
    type = sr.pushClass( name, isLocal, isAnonymous );

    if( superClass != null) {
      type.setSuperType( superClass.getType());
    }

    for( Iterator i = interfaces.listIterator(); i.hasNext() ; )
    {
      Type t = ((TypeNode)i.next()).getType();
      type.addInterface( t);
    }

    type.setAccessFlags( accessFlags);

    visitChildren(sr);

    sr.popClass();
    
    if ( accessFlags.isAbstract() || accessFlags.isInterface())
      return this;

    // if no constructor was declared in a Class, add the default constructor.
    boolean bHasConstructor = false;
    for ( Iterator i = type.getMethods().iterator() ; i.hasNext(); )
    {
      if ( i.next() instanceof ConstructorTypeInstance) 
      {
        bHasConstructor = true;
        break;
      }
    }
    if (!bHasConstructor)
    {
      AccessFlags afPublic = new AccessFlags();
      afPublic.setPublic(true);
      ((ParsedClassType)type).addMethod ( new ConstructorTypeInstance( 
                                                                      sr.getTypeSystem(), 
                                                                      type, 
                                                                      Collections.EMPTY_LIST, 
                                                                      Collections.EMPTY_LIST, 
                                                                      afPublic));
    }


    return this;
  }

  public void enterScope( LocalContext c)
  {
    c.pushClass( type);
  }

  public void leaveScope( LocalContext c)
  {
    c.popClass();
  }

  public Node cleanupSignatures( LocalContext c, SignatureCleaner sc)
    throws SemanticException, IOException
  {
    if (isAnonymous) {
      // If the class is anonymous, the parser created the node assuming
      // the super type is an interface, not a class.  After cleaning the
      // super type, if the assumption proves false, correct the mistake.

      if (type.getSuperType() != null || type.getInterfaces().size() != 1) {
	throw new InternalCompilerError("Anonymous classes should be " +
	  "constructed with a null superclass and one super-interface");
      }

      Type superType = (Type) type.getInterfaces().get(0);
      ClassType superClazz = (ClassType) c.getType(superType);

      //kliger: this block wasn't here before... bug(?)
      if (! sc.cleanPrerequisiteClass(superClazz)) {
	throw new SemanticException("Errors while compiling " +
				    "superclass " +
				    superClazz.getTypeString() +
				    " of "+ type.getTypeString() + "." +
				    Annotate.getLineNumber(this));
      }

      if (! superClazz.getAccessFlags().isInterface()) {
	type.setSuperType(superClazz);
	type.getInterfaces().clear();
      }
      else {
	type.setSuperType((ClassType) c.getTypeSystem().getObject());
      }
    }
    else {
      Type superType = type.getSuperType();

      if (superType != null) {
	ClassType superClazz = (ClassType) c.getType(superType);

	if (superClazz.getAccessFlags().isInterface()) {
	  throw new SemanticException("Superclass " +
	    superClazz.getTypeString() + " of " + type.getTypeString() +
	    " is an interface",
	    Annotate.getLineNumber(this));
	}

	//kliger: this block wasn't here before... bug(?)
	if (! sc.cleanPrerequisiteClass(superClazz)) {
	  throw new SemanticException("Errors while compiling " +
				      "superclass " +
				      superClazz.getTypeString() +
				      " of "+ type.getTypeString() + "." +
				      Annotate.getLineNumber(this));
	}

	type.setSuperType(superClazz);
      }
      else {
	type.setSuperType((ClassType) c.getTypeSystem().getObject());
      }

      for (ListIterator i = type.getInterfaces().listIterator(); i.hasNext();) {
	Type interfaceType = (Type) i.next();

	ClassType interfaceClazz = (ClassType) c.getType(interfaceType);

	if (! interfaceClazz.getAccessFlags().isInterface()) {
	  throw new SemanticException("Super-interface " +
	    interfaceClazz.getTypeString() + " of " + type.getTypeString() +
	    " is not an interface",
	    Annotate.getLineNumber(this));
	}

	if (! sc.cleanPrerequisiteClass(interfaceClazz)) {
	      throw new SemanticException("Errors while compiling " +
		  "super-interface " + interfaceClazz.getTypeString() +
		  " of " + type.getTypeString() + ".",
		  Annotate.getLineNumber(this) );
	}

	i.set(interfaceClazz);
      }
    }

    enterScope(c);

    ClassNode n = (ClassNode) visitChildren(sc);

    // We do methods and fields here because it's simpler to do it here than
    // in MethodNode or FieldNode.

    for (ListIterator i = n.type.getMethods().listIterator(); i.hasNext(); ) {
      MethodTypeInstance mti = (MethodTypeInstance) i.next();

      try {
	Type rt = mti.getReturnType();
	mti.setReturnType( c.getType(rt) );

	List argTypes = mti.argumentTypes();
	for (ListIterator j = argTypes.listIterator(); j.hasNext(); ) {
	  Type t = (Type) j.next();
	  j.set( c.getType(t) );
	}

	List excTypes = mti.exceptionTypes();
	for (ListIterator j = excTypes.listIterator(); j.hasNext(); ) {
	  Type t = (Type) j.next();
	  j.set( c.getType(t) );
	}
      } catch (SemanticException exn) {
	//rethrow with line number of current method type instance
	System.err.println("rethrowing a semantic exception");
	if (exn.getLineNumber() == SemanticException.INVALID_LINE) {
	  System.err.println("adjusting line number to be "+
			     Annotate.getLineNumber(mti));
	  throw new SemanticException(exn.getMessage(),
				      Annotate.getLineNumber(mti));
	}
      }
    }

    for (ListIterator i = n.type.getFields().listIterator(); i.hasNext(); ) {
      FieldInstance fi = (FieldInstance) i.next();

      try {
	Type t = fi.getType();
	fi.setType( c.getType(t) );
      } catch (SemanticException exn) {
	//rethrow with line number of current field instance
	if (exn.getLineNumber() == SemanticException.INVALID_LINE)
	  throw new SemanticException(exn.getMessage(),
				      Annotate.getLineNumber(fi));
      }
    }

    n.leaveScope(c);

    return n;
  }

  public Node removeAmbiguities( LocalContext c)
  {
    return this;
  }

  public Node typeCheck( LocalContext c)
  {
    // FIXME: implement;
    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( accessFlags.getStringRepresentation() + 
             (accessFlags.isInterface() ? "" : "class ") 
             + name);
    if( superClass != null)
    {
      w.write( " extends ");
      superClass.translate( c, w);
    }
    if( !interfaces.isEmpty())
    {
      w.write( " implements ");
      for( Iterator iter = interfaces(); iter.hasNext(); ) {   
        ((TypeNode)iter.next()).translate( c, w);
        if( iter.hasNext()) {
             w.write (", ");
        }
      }
    }

    translateBody(c, w);
  }

  public void translateBody( LocalContext c, CodeWriter w)
  {
    enterScope(c);
    w.newline( 0);
    w.write( "{");
    w.newline(4);
    w.begin(0);
    for( Iterator iter = members(); iter.hasNext(); ) {
      ((Node)iter.next()).translate_block( c, w);
      if( iter.hasNext()) {
        w.newline( 0);
      }
    }
    w.end();
    w.newline(0);
    w.write("}");
    w.newline(0);
    leaveScope(c);
  }
  
  public void dump( CodeWriter w) throws SemanticException
  {
    w.write( " ( CLASS < " + name + " >");
    w.write( " < " + accessFlags.getStringRepresentation() + "> ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}

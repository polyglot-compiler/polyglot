package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

import java.util.*;


/**
 * A <code>MethodNode</code> is an immutable representation of a method
 * definition as part of a class body.  It consists of a method name,
 * a list of formal parameters, a list of exceptions which may be
 * thrown, a return type, access flags, and a method body.  A method
 * may be a constructor in which case it does not have a name (as it
 * must be the same as the class, nor does it have a return type.)
 */
public class MethodNode extends ClassMember 
{
  protected final boolean isConstructor;
  protected final AccessFlags accessFlags;
  protected final TypeNode returns;
  protected final String name;
  protected final List formals;
  protected final List exceptions;
  protected final BlockStatement body;
  protected final int addDims;
 
  // FIXME blah...
  private MethodTypeInstance mtiThis; 

  /**
   * Creates a new <code>MethodNode</code> that represents the definition of 
   * a constructor.
   *
   * @pre Requires that each element of <code>formals</code> is of type
   *  <code>FormalParameter</code> and that each element of 
   *  <code>exceptions</code> is of type <code>TypeNode</code>.
   */
  public MethodNode( AccessFlags accessFlags, TypeNode tn, List formals, 
                     List exceptions, BlockStatement body) 
  {
    this.isConstructor = true;
    this.accessFlags = accessFlags;
    this.returns = tn;
    this.name = null;
    this.formals = TypedList.copyAndCheck( formals, FormalParameter.class, 
                                           true);
    this.exceptions = TypedList.copyAndCheck( exceptions, TypeNode.class,
                                              true);
    this.body = body;
    this.addDims = 0;

    mtiThis = null;
  }

  /**
   * Creates a new <code>MethodNode</code> that represents the definition of 
   * an ordinary (non-constructor) method.
   *
   * @pre Requires that each element of <code>formals</code> is of type
   *  <code>FormalParameter</code> and that each element of 
   *  <code>exceptions</code> is of type <code>TypeNode</code>.
   */ 
  public MethodNode( AccessFlags accessFlags, TypeNode returns, String name,
                     List formals, List exceptions, BlockStatement body)
  {
    this( accessFlags, returns, name, formals, exceptions, body, 0);
  }

  /**
   * As above, except this constructor takes an additional, and 
   * infrequently used, parameter. This parameter may be used for definitions
   * like this:
   * <pre><code>
   * public int foo()[]
   * {
   *   return new int[13];
   * }
   * </code></pre>
   *
   * @param addDims The number of additional dimensions of the 
   *  return type.
   */
  public MethodNode( AccessFlags accessFlags, TypeNode returns, String name,
                     List formals, List exceptions, BlockStatement body,
                     int addDims)
  {
    this.isConstructor = false;
    this.accessFlags = accessFlags;
    this.returns = returns;
    this.name = name;
    this.formals = TypedList.copyAndCheck( formals, FormalParameter.class, 
                                           true);
    this.exceptions = TypedList.copyAndCheck( exceptions, TypeNode.class,
                                              true);
    this.body = body;
    this.addDims = 0;

    mtiThis = null;
  }

  /**
   * Lazily reconstruct this node.
   */
  public MethodNode reconstruct( boolean isConstructor, 
                                 AccessFlags accessFlags, TypeNode returns,
                                 String name, List formals, List exceptions,
                                 BlockStatement body, int addDims)
  {
    if( this.isConstructor != isConstructor 
        || !this.accessFlags.equals( accessFlags) 
        || this.returns != returns 
        || (this.name == null && name != null) 
        || (this.name != null && !this.name.equals( name))
        || this.formals.size() != formals.size() 
        || this.exceptions.size() != exceptions.size() 
        || this.body != body 
        || this.addDims != addDims) {
      MethodNode n = (isConstructor ? 
                      new MethodNode( accessFlags, returns, formals, 
                                      exceptions, body) :
                      new MethodNode( accessFlags, returns, name, 
                                      formals, exceptions, body, 
                                      addDims));
      n.mtiThis = mtiThis;
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < formals.size(); i++) {
        if( this.formals.get( i) != formals.get( i)) {
          MethodNode n = (isConstructor ? 
                          new MethodNode( accessFlags, returns, formals, 
                                          exceptions, body) :
                          new MethodNode( accessFlags, returns, name, 
                                          formals, exceptions, body, 
                                          addDims));
          n.copyAnnotationsFrom( this);
          n.mtiThis = mtiThis;
          return n;
        }
      }
          
      for( int i = 0; i < exceptions.size(); i++) {
        if( this.exceptions.get( i) != exceptions.get( i)) {
          MethodNode n = (isConstructor ? 
                          new MethodNode( accessFlags, returns, formals, 
                                          exceptions, body) :
                          new MethodNode( accessFlags, returns, name, 
                                          formals, exceptions, body, 
                                          addDims));
          n.copyAnnotationsFrom( this);
          n.mtiThis = mtiThis;
          return n;
        }
      }
      
      return this;
    }
  }

  /**
   * Returns <code>true</code> iff this node represents a constructor.
   */
  public boolean isConstructor() 
  {
    return isConstructor;
  }

  /**
   * Returns the <code>AccessFlags</code> modifing this node.
   */
  public AccessFlags getAccessFlags() 
  {
    return accessFlags;
  }

  /**
   * Returns the return type of this method.
   *
   * @pre This node must define an method which is <b>not</b> a constructor.
   */
  public Type getReturnType() 
  {
    if( addDims != 0) {
      try {
        return returns.getType().extendArrayDims( addDims);
      }
      catch( SemanticException e)
      {
        throw new InternalCompilerError( e.toString());
      }
    }
    else {
      return returns.getType();
    }
  }

  public TypeNode getReturnTypeNode()
  {
    return returns;
  }

  /**
   * Returns the name of this this method.
   *
   * @pre This node must define an method which is <b>not</b> a constructor.
   */
  public String getName() 
  {
    return name;
  }

  /**
   * Returns the parameter of this method as position <code>pos</code>.
   */
  public FormalParameter getFormalParameterAt( int pos) 
  {
    return (FormalParameter)formals.get( pos);
  }

  /**
   * Returns a iterator which produces the formal parameters in order of the
   * method defined by this node.
   *
   * @post Each member of the iterator is of type <code>FormalParameter</code>.
   */
  public Iterator formalParameters()
  {
    return formals.iterator();
  }

  /**
   * Returns the exception at position <code>pos</code> in the exception list.
   */
  public TypeNode getExceptionAt( int pos) 
  {
    return (TypeNode)exceptions.get( pos);
  }

  /**
   * Returns a iterator which returns the exceptions thrown by this method
   * (in order).
   *
   * @post Each member of the iterator is of type <code>TypeNode</code>.
   */
  public Iterator exceptions() 
  {
    return exceptions.iterator();
  }

  /**
   * Returns the block representing the body of this method.
   */
  public BlockStatement getBody() 
  {
    return body;
  }
  
  Node visitChildren( NodeVisitor v) 
  {
    TypeNode newReturns = (TypeNode)returns.visit( v);

    List newFormals = new ArrayList( formals.size()),
      newExceptions = new ArrayList( exceptions.size());

    for( Iterator iter = formalParameters(); iter.hasNext(); ) {
      FormalParameter fp = (FormalParameter)((FormalParameter)iter.next())
                                                                .visit( v);
      if( fp != null) {
        newFormals.add( fp);
      }
    }

    for( Iterator iter = exceptions(); iter.hasNext(); ) {
      TypeNode tn = (TypeNode)((TypeNode)iter.next()).visit( v);
      if( tn != null) {
        newExceptions.add( tn);
      }
    }

    BlockStatement newBody = null;

    if( body != null) {
      newBody = (BlockStatement)body.visit( v); 
    }

    return reconstruct( isConstructor, accessFlags, newReturns, name,
                        newFormals, newExceptions, newBody,
                        addDims);
  }

  public Node readSymbols( SymbolReader sr)
  {
    ParsedClassType clazz = sr.getCurrentClass();
    TypeSystem ts = sr.getTypeSystem();

    /* Build a list of argument types. */
    List argTypes = new LinkedList();
    List excTypes = new LinkedList(); 

    Iterator iter = formals.iterator();
    while( iter.hasNext()) {
      argTypes.add( ((FormalParameter)iter.next()).getParameterType());
    }

    for ( iter = exceptions.iterator(); iter.hasNext() ; )
    {
      excTypes.add ( (((TypeNode)iter.next()).getType()));
    }
    
    if ( isConstructor)
    {
      mtiThis = new ConstructorTypeInstance( ts, clazz, argTypes, 
                                             excTypes, accessFlags) ;
    }
    else if( addDims == 0 ) {
      mtiThis = new MethodTypeInstance( ts, clazz, name,
                          returns.getType(), argTypes, 
                          excTypes, accessFlags);
    }
    else {
      mtiThis = new MethodTypeInstance( ts, clazz, name,
                          new ArrayType( ts, returns.getType(), 
                                         addDims),
                          argTypes, excTypes, accessFlags);     
    }

    Annotate.setLineNumber( mtiThis, Annotate.getLineNumber( this));
    clazz.addMethod( mtiThis);

    return this;
  }

  public void enterScope( LocalContext c)
  {
    c.enterMethod( mtiThis);
  }

  public void leaveScope( LocalContext c)
  {
    c.leaveMethod();
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    return this;
  }

  public Node exceptionCheck (ExceptionChecker ec ) throws SemanticException
  {
    SubtypeSet s = (SubtypeSet)ec.getThrowsSet();
    // check our exceptions:
    for (Iterator i = s.iterator(); i.hasNext() ; )
    {
      boolean bThrowDeclared = false;
      Type t = (Type)i.next();
      
      if ( !t.isUncheckedException() )
      {
        for (Iterator i2 = exceptions.iterator(); i2.hasNext() ; )
        {
          Type t2 =  (ClassType)  ((TypeNode)i2.next()).getType();
          if ( t.equals (t2) || t.descendsFrom (t2 ))
          {
            bThrowDeclared = true; 
            break;
          }
        }
        if ( ! bThrowDeclared)
          ec.reportError("Method \"" + name + "\" throws the undeclared "
                         + "exception \"" + t.getTypeString() + "\".", 
                         Annotate.getLineNumber( this ) );
      }
    }
    ec.getThrowsSet().clear();
    return this;
  }    

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( accessFlags.getStringRepresentation());
    if( !isConstructor()) {
      returns.translate( c, w);
      w.write( " " + name + "( ");
    }
    else {
      returns.translate( c, w);
      w.write( "( ");
    }

    for( Iterator iter = formals.iterator(); iter.hasNext(); )
    {
      ((FormalParameter)iter.next()).translate(c, w);
      if( iter.hasNext()) {
        w.write( ", ");
      }
    }
    w.write( ")");

    if( addDims > 0) {
      for( int i = 0; i < addDims; i++) {
        w.write( "[]");
      }
    }

    if( !exceptions.isEmpty()) {
      w.write( " throws ");
      for( Iterator i = exceptions.iterator(); i.hasNext(); ) {
        w.write ( ((TypeNode)i.next()).getType().getTypeString());
        w.write ( (i.hasNext() ? ", " : "" ));
      }
    }
    
    if( !mtiThis.getAccessFlags().isAbstract() ) {
      // FIXME should be abstract for interfaces.
      if( body != null) {
        w.newline( 0);
        body.translate(c, w);
      }
      else {
        w.write( ";");
      }
    }
    else {
      w.write( ";");
    }
  }

  public void dump( CodeWriter w)
  {
    w.write( "( METHOD");
    w.write( " < " + name + " >");
    w.write( " < " + accessFlags.getStringRepresentation() + "> ");
    if( isConstructor) {
      w.write( "< isConstructor > ");
    }
    if( addDims > 0) {
      w.write( "< " + addDims + " > ");
    }
    dumpNodeInfo( w);
    w.write( ")");
  }
}

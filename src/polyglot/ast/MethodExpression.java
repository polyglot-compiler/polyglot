package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import java.util.*;


/**
 * A <code>MethodExpression</code> is an immutable representation of a Java
 * method call.  It consists of a method name and a list of arguments.
 * It may also have either a Type upon which the method is being
 * called or an expression upon which the method is being called.
 */
public class MethodExpression extends Expression 
{
  protected final Node target;
  protected final String name;
  protected final List args;

  /**
   * FIXME: Another occasion where we have a muttable field
   */
  MethodTypeInstance mti;

  /**
   * Create a new <code>MethodExpression</code>.
   * 
   * @pre Requires that <code>target</code> is either a <code>TypeNode</code>
   *  or an <code>Expression</code> or <code>null</code>. Also 
   *  <code>args</code> must be a list of <code>Expression</code>s.
   */
  public MethodExpression( Node target, String name, List args) 
  {
    if (target != null && ! (target instanceof TypeNode ||
			     target instanceof Expression))
      throw new Error("Target of a method call must be a type or expression.");

    this.target = target;
    this.name = name;
    this.args = TypedList.copyAndCheck( args, Expression.class, true);
  }
  
  /**
   * Lazily reconstruct this node.
   */
  public MethodExpression reconstruct( Node target, String name, List args)
  {
    if( this.target != target || !this.name.equals( name) 
        || this.args.size() != args.size()) {
      MethodExpression n = new MethodExpression( target, name, args);
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < args.size(); i++) {
        if( this.args.get( i) != args.get( i)) {
          MethodExpression n = new MethodExpression( target, name, args);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      return this;
    }
  }

  /**
   * Returns the target that that the method is being called on.
   *
   * @post The result may be null if there is method is defined in this class
   *  a super class, or a containing class. Otherwise, the target will be a
   *  <code>TypeNode</code> or an <code>Expression</code>.
   */
  public Node getTarget() 
  {
    return target;
  }
 
  /**
   * Returns the name of the method being called in this node.
   */
  public String getName() 
  {
    return name;
  }

  /**
   * Returns the argument at position <code>pos</code> of the method being 
   * called. 
   */
  public Expression getArgumentAt( int pos) 
  {
    return (Expression)args.get( pos);
  }

  /**
   * Returns an Iterator which will produce the arguments of this invocation
   * in order.
   */
  public Iterator arguments() 
  {
    return args.iterator();
  }
  
  /**
   * Visit the children of this node.
   */
  Node visitChildren( NodeVisitor v) 
  {
    Node newTarget = null;

    if( target != null) {
      newTarget = target.visit( v);
    }
    
    List newArgs = new ArrayList( args.size());
    
    for( Iterator iter = arguments(); iter.hasNext(); ) {
      Expression expr = (Expression)((Expression)iter.next()).visit( v);
      if( expr != null) {
        newArgs.add( expr);
      }
    }

    return reconstruct( newTarget, name, newArgs);
  }

  public Node removeAmbiguities( LocalContext c)
  {
    //FIXME: should this be rectified in the grammar and not here?
    // i.e, should name always come in as a short name?
    if( !c.getTypeSystem().getPackageComponent(name).equals( ""))
    {
      Node n = reconstruct( new AmbiguousNameExpression( 
                                c.getTypeSystem().getPackageComponent( name)),
                          c.getTypeSystem().getShortNameComponent( name),
                          args);
      return n.visit( c.getVisitor());
    }
    return this;
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    ClassType ct; 

    if( target == null) {
      //      ct = c.getCurrentClass();
      ct = null;
    }
    else if( target instanceof TypeNode 
                && ((TypeNode)target).getType() instanceof ClassType) {
      ct = (ClassType)((TypeNode)target).getType();
    }
    else if( target instanceof Expression) {
      if( ((Expression)target).getCheckedType() instanceof ClassType) {
        ct = (ClassType)((Expression)target).getCheckedType();
      }
      else if( ((Expression)target).getCheckedType() instanceof ArrayType) {
        ct = (ClassType)c.getTypeSystem().getObject();
      }
      else {
        throw new SemanticException( "Cannot invoke method \""
                           + name + "\" on an expression of primitive type.");
      }
    }
    else {
      throw new SemanticException( 
                         "Target of method invocation must be a class type.");
    }

    List argTypes = new ArrayList( args.size());
    for( Iterator iter = arguments(); iter.hasNext(); ) {
      argTypes.add( ((Expression)iter.next()).getCheckedType());
    }

    mti = c.getMethod( ct, name, argTypes);
    setCheckedType( mti.getType());

    /* 
     * SPECIAL CASE EXCEPTION
     * ArrayType.clone should not throw the CloneNotSupportedException, 
     * as per JLS 10.7.
     */
    /*
      //FIXME don't forget this in exceptionCheck
    if( !(target instanceof Expression &&
            ((Expression)target).getCheckedType() instanceof ArrayType &&
          name.equals( "clone"))) {
      jltools.util.Annotate.addThrows( this, mti.exceptionTypes());
    }
    */

    List formalTypes = mti.argumentTypes();
    for( Iterator iter1 = arguments(),
            iter2 = formalTypes.iterator(); iter1.hasNext(); ) {
       ((Expression)iter1.next()).setExpectedType( (Type)iter2.next());
    }

    return this;
  }

  public Node exceptionCheck( ExceptionChecker ec) 
    throws SemanticException 
  {
    // something went wrong in the typecheck phase. so don't do exception checking.
    if (mti == null) 
      return this;

    for (Iterator i = mti.exceptionTypes().iterator(); i.hasNext(); )
    {
      ec.throwsException( (ClassType)i.next() );
    }
    return this;
  }
  
  public void translate( LocalContext c, CodeWriter w)
  {
    if( target != null) {
      if( target instanceof Expression) {
        translateExpression( (Expression)target, c, w);
      }
      else {
        target.translate( c, w);
      }
      w.write( ".");
    }

    w.write( name + "(");
    w.begin(0);
    
    for( Iterator iter = arguments(); iter.hasNext(); ) {
      ((Expression)iter.next()).translate( c, w);
      if (iter.hasNext()) {
        w.write( ",");
	w.allowBreak(2, " ");
      }
    }
    w.end();
    w.write( ")");
  }
  
  public void dump( CodeWriter w)
  {
    w.write( " ( INVOCATION");
    w.write( " < " + name + " > ");
    dumpNodeInfo( w);
    w.write( ")");
  }

  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }
}
    
  

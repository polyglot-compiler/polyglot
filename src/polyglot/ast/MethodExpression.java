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

    public MethodTypeInstance getMTI() {
	return mti;
    }

    public void setMTI(MethodTypeInstance mti) {
	this.mti = mti;
    }

  /**
   * Create a new <code>MethodExpression</code>.
   * 
   * @pre Requires that <code>target</code> is either a <code>TypeNode</code>
   *  or an <code>Expression</code> or <code>null</code>. Also 
   *  <code>args</code> must be a list of <code>Expression</code>s.
   */
  public MethodExpression( Node ext, Node target, String name, List args) 
  {
    if (target != null && ! (target instanceof AmbiguousNode ||
			     target instanceof TypeNode ||
			     target instanceof Expression))
      throw new Error("Target of a method call must be an ambiguous node, a type, or an expression.");
    this.ext = ext;
    this.target = target;
    this.name = name;
    this.args = TypedList.copyAndCheck( args, Expression.class, true);

    if (! TypeSystem.isNameShort(name)) {
      throw new InternalCompilerError("Method name \"" + name +
	  "\" should be short.");
    }
  }
  
    public MethodExpression( Node target, String name, List args) {
	this(null, target, name, args);
    }

  /**
   * Lazily reconstruct this node.
   */
  public MethodExpression reconstruct( Node ext, Node target, String name, List args)
  {
    if( this.target != target || this.ext != ext || !this.name.equals( name) 
        || this.args.size() != args.size()) {
      MethodExpression n = new MethodExpression( ext, target, name, args);
      n.copyAnnotationsFrom( this);
      n.mti = this.mti;
      return n;
    }
    else {
      for( int i = 0; i < args.size(); i++) {
        if( this.args.get( i) != args.get( i)) {
          MethodExpression n = new MethodExpression( ext, target, name, args);
          n.copyAnnotationsFrom( this);
	  n.mti = this.mti;
          return n;
        }
      }
      return this;
    }
  }

    public MethodExpression reconstruct( Node target, String name, List args) {
	return reconstruct(this.ext, target, name, args);
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

    public List getArgs() {
	return args;
    }
  
  /**
   * Visit the children of this node.
   */
  public Node visitChildren( NodeVisitor v) 
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

    return reconstruct( Node.condVisit(this.ext, v),newTarget, name, newArgs);
  }

  public Node removeAmbiguities( LocalContext c)
  {
    //FIXME: should this be rectified in the grammar and not here?
    // i.e, should name always come in as a short name?
    if( !c.getTypeSystem().getPackageComponent(name).equals( ""))
    {
      Node n = reconstruct( this.ext, new AmbiguousNameExpression( 
                                c.getTypeSystem().getPackageComponent( name)),
                          c.getTypeSystem().getShortNameComponent( name),
                          args);
      return n.visit( c.getVisitor());
    }
    return this;
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    ReferenceType ct; 

    if( target == null) {
      //      ct = c.getCurrentClass();
      ct = null;
    }
    else if( target instanceof TypeNode 
                && ((TypeNode)target).getType().isReferenceType()) {
      ct = ((TypeNode)target).getType().toReferenceType();
    }
    else if( target instanceof Expression) {
      if( ((Expression)target).getCheckedType().isReferenceType()) {
        ct = ((Expression)target).getCheckedType().toReferenceType();
      }
      else {
        throw new SemanticException( "Cannot invoke method \""
			 + name + "\" on an expression of non-reference type.",
				  Annotate.getLineNumber(target));
      }
    }
    else {
      throw new SemanticException( 
		       "Target of method invocation must be a reference type.",
				  Annotate.getLineNumber(target));
    }

    List argTypes = new ArrayList( args.size());
    for( Iterator iter = arguments(); iter.hasNext(); ) {
      argTypes.add( ((Expression)iter.next()).getCheckedType());
    }

    mti = c.getMethod( ct, name, argTypes);
    setCheckedType( mti.getType());

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

    TypeSystem ts = ec.getTypeSystem();

    if (target instanceof Expression &&
        ! (target instanceof SpecialExpression)) {
        ec.throwsException((ClassType) ts.getNullPointerException());
    }

    return this;
  }
  
  public void translate_no_override( LocalContext c, CodeWriter w)
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

    w.write(name + "(");
    w.begin(0);
    
    for( Iterator iter = arguments(); iter.hasNext(); ) {
      ((Expression)iter.next()).translate( c, w);
      if (iter.hasNext()) {
        w.write(",");
	w.allowBreak(0);
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

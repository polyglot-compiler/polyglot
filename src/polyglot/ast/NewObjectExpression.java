package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import java.util.*;


/**
 * A NewObjectExpression is an immutable representation of the
 * use of the <code>new</code> operator to create a new instance of a class.  
 * In addition to the type of the class being created, a
 * <code>NewObjectExpression</code> has a list of arguments to be passed to the
 * constructor of the object and an optional <code>ClassNode</code> used to
 * support anonymous classes. Such an expression may also be proceeded
 * by an primary expression which specifies the context in which the
 * object is being created.
 */

public class NewObjectExpression extends Expression 
{
  protected final Expression primary;
  protected final TypeNode tn;
  protected final List args;
  protected final ClassNode cn;

  /**
   * FIXME: Another occasion where we have a muttable field
   */
  MethodTypeInstance mti;

    public MethodTypeInstance getMTI() {
	return mti;
    }

  /**
   * Creates a new <code>NewObjectExpression</code>.
   *
   * @pre Requires that each element of <code>args</code> is an object of type
   *  <code>Expression</code>.
   */
  public NewObjectExpression( Node ext, Expression primary, TypeNode tn,
                              List args, ClassNode cn)
  {
    this.ext = ext;
    this.primary = primary;
    this.tn = tn;
    this.args = TypedList.copyAndCheck( args, Expression.class, true);
    this.cn = cn;
  }

  public NewObjectExpression( Expression primary, TypeNode tn,
                              List args, ClassNode cn) {
      this(null, primary, tn, args, cn);
  }


  /**
   * Lazily reconstruct this node. 
   */
  public NewObjectExpression reconstruct( Node ext, Expression primary, TypeNode tn,
                                          List args, ClassNode cn)
  {
    if( this.primary != primary || this.tn != tn || this.ext != ext
        || this.args.size() != args.size() || this.cn != cn) {
      NewObjectExpression n = new NewObjectExpression( ext, primary, tn,
                                                       args, cn);
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < args.size(); i++) {
        if( this.args.get( i) != args.get( i)) {
          NewObjectExpression n = new NewObjectExpression( ext, primary, tn,
                                                           args, cn);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      return this;
    }    
  }

  public NewObjectExpression reconstruct( Expression primary, TypeNode tn,
                                          List args, ClassNode cn) {
      return reconstruct(this.ext, primary, tn, args, cn);
  }


  /**
   * Returns the primary expression of this node or <code>null</code> if there
   * is none.
   */
  public Expression getPrimary() 
  {
    return primary;
  }

  /**
   * Returns the type of the object being created by this 
   * <code>NewObjectExpression</code>.
   */
  public Type getType() 
  {
    return tn.getType();
  }
  
  public TypeNode getTypeNode() {
	  return tn;
  }

  /**
   * Returns the argument at position <code>pos</code>.
   */
  public Expression getArgumentAt( int pos) 
  {
    return (Expression)args.get( pos);
  }

  /**
   * Returns an iterator which will yield each argument exprssion of this 
   * in order.
   */
  public Iterator arguments() 
  {
    return args.iterator();
  }

    public List getArgs()
    {
	return args;
    }

  /**
   * Returns the <code>ClassNode</code> defining the anonymous class being
   * created. If no such class exits, returns <code>null</code>.
   */
  public ClassNode getClassNode() 
  {
    return cn;
  }

  /**
   * Requires: v will not transform the an Expression from the
   * argument list into anything other than another expression and
   * will not transform a ClassMemeber of the class body into
   * anything other than another ClassMemeber.
   *
   * Effects: visits each child of this with <v>.  If <v> returns null
   * for a memeber of the class body, then that member is removed. 
   */
  public Node visitChildren( NodeVisitor v) 
  {
    Expression newPrimary = null;
    TypeNode newTn = tn;

    if( primary != null) {
      newPrimary = (Expression)primary.visit( v);
    } else {
      /**
       * avoid visiting tn if the primary is non-null.  In that case,
       * tn does not contain any meaningful (besides an identifier)
       * information since it refers to a class which is an inner of
       * the compile-time type of the primary.
       * So it only becomes meaningful after typechecking, when we
       * learn the actual class that it refers to.
       */
      newTn = (TypeNode)tn.visit( v);
    }

    List newArgs = new ArrayList( args.size());

    for( Iterator iter = arguments(); iter.hasNext(); ) {
      Expression expr = (Expression)((Expression)iter.next()).visit( v);

      if( expr != null) {
        newArgs.add( expr);
      }
    }

    ClassNode newCn = null;

    if( cn != null) {
      newCn = (ClassNode)cn.visit( v);
    }

    return reconstruct( Node.condVisit(this.ext, v),newPrimary, newTn, newArgs, newCn);
  }
  
  public Node typeCheck( LocalContext c) throws SemanticException
  {
    /* if there is a primary expression, do a late-lookup of
     * the identifier to find the class that we are instantiating
     * here.
     */
    ClassType ct;
    if (primary != null) {
      ct = (ClassType)
	c.getTypeSystem().checkAndResolveType(tn.getType(),
					      primary.getCheckedType());
    } else
      ct = (ClassType) tn.getCheckedType();

    // make sure that primary is the "containing" class for the inner class, 
    // if appropriate
    if( primary != null && 
        !primary.getCheckedType().equals(
	ct.getContainingClass())) {
      throw new SemanticException (
              "The containing instance must be the containing class of \"" +
              tn.getType().getTypeString() + "\".",
	      Annotate.getLineNumber(primary));
    }

    if( primary != null && 
         ct.getAccessFlags().isStatic()) {
      // FIXME is this really true?
      throw new SemanticException(
             "Cannot specify a containing instance for static classes.",
	      Annotate.getLineNumber(primary));
    }

    if( ct.getAccessFlags().isAbstract()) {
      throw new SemanticException( "Cannot instantiate an abstract class.",
	      Annotate.getLineNumber(this));
    }

    List argTypes = new ArrayList( args.size());
    for( Iterator iter = arguments(); iter.hasNext(); ) {
      argTypes.add( ((Expression)iter.next()).getCheckedType());
    }

    // We could be creating an anonymous class which implements an interface.
    // An empty argument list is okay in this case.
    if (cn != null &&
	ct.getAccessFlags().isInterface() &&
	argTypes.size() == 0) {

      setCheckedType( ct);
      return this;
    }

    mti = null;
    try
    {
//        mti = c.getMethod( ct, new ConstructorType( c.getTypeSystem(), 
//                                                    ct, 
//                                                    argTypes));
      mti = c.getTypeSystem().getConstructor(ct, argTypes, c);
    }
    catch (SemanticException e)
    {
      /*
       * FIXME what does this do?
      for( Iterator iter = argTypes.iterator(); iter.hasNext() ; ) {
        Type t = (Type)i.next();
      } */
      System.out.println( ct.getTypeString() );
      throw new SemanticException ( 
              "No acceptable constructor found for the creation of \"" 
              + ct.getTypeString() + "\".",
	      Annotate.getLineNumber(this));
    }
    setCheckedType( ct);

    List formalTypes = mti.argumentTypes();
    for( Iterator iter1 = arguments(),
            iter2 = formalTypes.iterator(); iter1.hasNext(); )
    {
       ((Expression)iter1.next()).setExpectedType( (Type)iter2.next());
    }

    TypeNode newTn = tn.reconstruct(ct, tn.getOriginal());
    newTn.setCheckedType(ct);
    return reconstruct(primary, newTn, args, cn);
  }

  public Node exceptionCheck( ExceptionChecker ec) 
    throws SemanticException 
  {
    // something didn't work in the type check phase, so just ignore it.
    if (mti == null) 
      return this; 

    for (Iterator i = mti.exceptionTypes().iterator(); i.hasNext(); )
    {
      Type o = (Type)i.next();
      ClassType ct = null;

      if ( o.isClassType()) ct = o.toClassType();
      else throw new InternalCompilerError("The constructor throws " +
                                           " exceptions that are not of "
                                           + "ClassType");

      ec.throwsException( ct );
    }
    return this;
  }

  
  public void translate( LocalContext c, CodeWriter w)
  {
    if( primary != null)
    {
      primary.translate( c, w);
      w.write( ".new ");
    }
    else {
      w.write( "new ");
    }

    if (primary != null) {
      w.write( ((ClassType)tn.getCheckedType()).getShortName() );
    } else {
      tn.translate( c, w);
    }
    w.write( "("); w.begin(0);

    for( Iterator iter = arguments(); iter.hasNext(); ) {
      ((Expression)iter.next()).translate( c, w);
      if(iter.hasNext()) {
        w.write(",");
	w.allowBreak(0);
      }
    }

    w.end(); w.write(")");

    if( cn != null) {
      cn.translateBody(c, w);
    }
  }

  public void dump( CodeWriter w)
  {
    w.write ("NEW " );
    dumpNodeInfo( w);
  }
  
  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }
}
  

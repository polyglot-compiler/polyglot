package jltools.ast;

import jltools.types.*;
import jltools.util.*;

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
   * Creates a new <code>NewObjectExpression</code>.
   *
   * @pre Requires that each element of <code>args</code> is an object of type
   *  <code>Expression</code>.
   */
  public NewObjectExpression( Expression primary, TypeNode tn,
                              List args, ClassNode cn)
  {
    this.primary = primary;
    this.tn = tn;
    this.args = TypedList.copyAndCheck( args, Expression.class, true);
    this.cn = cn;
  }

  /**
   * Lazily reconstruct this node. 
   */
  public NewObjectExpression reconstruct( Expression primary, TypeNode tn,
                                          List args, ClassNode cn)
  {
    if( this.primary != primary || this.tn != tn
        || this.args.size() != args.size() || this.cn != cn) {
      NewObjectExpression n = new NewObjectExpression( primary, tn,
                                                       args, cn);
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < args.size(); i++) {
        if( this.args.get( i) != args.get( i)) {
          NewObjectExpression n = new NewObjectExpression( primary, tn,
                                                           args, cn);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      return this;
    }    
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
  Node visitChildren( NodeVisitor v) 
  {
    Expression newPrimary = null;

    if( primary != null) {
      newPrimary = (Expression)primary.visit( v);
    }

    TypeNode newTn = (TypeNode)tn.visit( v);

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

    return reconstruct( newPrimary, newTn, newArgs, newCn);
  }
  
  public Node typeCheck( LocalContext c) throws SemanticException
  {
    // make sure that primary is the "containing" class for the inner class, 
    // if appropriate
    if( primary != null && 
        !primary.getCheckedType().equals((ClassType)tn.getType())) {
      throw new SemanticException (
              "The containing instance must be the containing class of \"" +
              tn.getType().getTypeString() + "\".");
    }

    if( primary != null && 
         ((ClassType)tn.getType()).getAccessFlags().isStatic()) {
      // FIXME is this really true?
      throw new SemanticException(
             "Cannot specify a containing instance for static classes.");
    }

    if( ((ClassType)tn.getType()).getAccessFlags().isAbstract()) {
      throw new SemanticException( "Cannot instantiate an abstract class.");
    }

    ClassType ct; 
    ct = (ClassType)tn.getType();

    List argTypes = new ArrayList( args.size());
    for( Iterator iter = arguments(); iter.hasNext(); ) {
      argTypes.add( ((Expression)iter.next()).getCheckedType());
    }

    MethodTypeInstance mti = null;
    try
    {
      mti = c.getMethod( ct, new ConstructorType( c.getTypeSystem(), 
                                                  argTypes));
    }
    catch (SemanticException e)
    {
      /*
       * FIXME what does this do?
      for( Iterator iter = argTypes.iterator(); iter.hasNext() ; ) {
        Type t = (Type)i.next();
      }
      */
      throw new SemanticException ( 
              "No acceptable constructor found for the creation of \"" 
              + ct.getTypeString() + "\".");
    }
    setCheckedType( ct);

    List formalTypes = mti.argumentTypes();
    for( Iterator iter1 = arguments(),
            iter2 = formalTypes.iterator(); iter1.hasNext(); )
    {
       ((Expression)iter1.next()).setExpectedType( (Type)iter2.next());
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

    tn.translate( c, w);
    w.write( "( ");

    for( Iterator iter = arguments(); iter.hasNext(); ) {
      ((Expression)iter.next()).translate( c, w);
      if(iter.hasNext()) {
        w.write(", ");
      }
    }

    w.write( ")");

    if( cn != null) {
      cn.translate(c, w);
    }
  }

  public void dump( CodeWriter w)
  {
    w.write ("( NEW " );
    dumpNodeInfo( w);
    w.write( ")");
  }
  
  public int getPrecedence()
  {
    return PRECEDENCE_OTHER;
  }
}
  

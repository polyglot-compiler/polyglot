package jltools.ast;

import jltools.util.*;
import jltools.types.*;

import java.util.*;


/**
 * A <code>ConstructorCallStatement</code> is an immutable representation of
 * a direct call to a constructor of a class in the form of
 * <code>super(...)</code>  or <code>this(...)</code>.  It consists of the
 * kind of the call (either <code>super</code> or <code>this</code> and a list
 * of expressions to be parameters of the call.  A constructor call statement
 * may also contain an expression providing the context in which it is 
 * executed.
 */
public class ConstructorCallStatement extends Statement
{
  public static final int SUPER   = 0; 
  public static final int THIS    = 1;

  // the highest kind used by this class
  protected static final int MAX_KIND = THIS; 

  protected final Expression primary;
  protected final int kind;
  protected final List arguments;

  /**
   * Creates a new <code>ConstructorCallStatement</code>.
   *
   * @pre <code>kind</code> must be one of the valid kinds listed in the 
   *  <code>static final int</code>s in this class. Also, 
   *  <code>arguments</code> must be a list of <code>Expression</code>.
   */
  public ConstructorCallStatement( Node ext, Expression primary,
				  int kind, List arguments) 
  {
    if (kind < 0 || kind > MAX_KIND) {
      throw new IllegalArgumentException( "Value for kind of " +
                                          "ConstructorCallStatement " +
                                          "is not valid.");
    }
    this.ext = ext;
    this.kind = kind;
    this.primary = primary;
    this.arguments = TypedList.copyAndCheck( arguments,
                                             Expression.class, 
                                             true);
  }

  public ConstructorCallStatement( Expression primary,
				   int kind, List arguments) {
      this(null, primary, kind, arguments);
  }


  /**
   * Lazily reconstruct this node.
   */
  public ConstructorCallStatement reconstruct( Node ext, Expression primary, int kind,
                                               List arguments)
  {
    if( this.primary != primary || this.kind != kind || this.ext != ext
        || this.arguments.size() != arguments.size()) {
      ConstructorCallStatement n = new ConstructorCallStatement( ext, primary, 
                                                                 kind, 
                                                                 arguments);
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < arguments.size(); i++) {
        if( this.arguments.get( i) != arguments.get( i)) {
          ConstructorCallStatement n = new ConstructorCallStatement( ext, primary, 
                                                                     kind, 
                                                                    arguments);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      return this;
    }
  }

  public ConstructorCallStatement reconstruct( Expression primary, int kind,
                                               List arguments)
    {
	return reconstruct(this.ext, primary, kind, arguments);
    }


  /**
   * Returns the expression providing the contect in which this constructor is
   * executed or <code>null</code> if none. 
   */
  public Expression getPrimary() 
  {
    return primary;
  }

  /**
   * Returns the kind of constructor call as defined in the list of public
   * static <code>int</code>s in this class.
   */
  public int getKind() 
  {
    return kind;
  }   

  /**
   * Returns the argument at position <code>pos</code>.  
   */
  public Expression getArgumentAt( int pos) 
  {
    return (Expression) arguments.get(pos);
  }

  /**
   * Returns an <code>Iterator</code> which will yield each expression which is
   * an argument in this call in order.
   */
  public Iterator arguments() 
  {
    return arguments.iterator();
  }

    public List getArgs() {
	return arguments;
    }

  /**
   * Visit the children of this node.
   *
   * @pre Requires that <code>primary.visit</code> and <code>visit</code> for
   *  each of the elements of <code>arguments</code> return object of type
   *  <code>Expression</code>.
   */
  public Node visitChildren(NodeVisitor v) 
  {
    Expression newPrimary = null;

    if (primary != null) {
      newPrimary = (Expression)primary.visit( v);
    }
    
    List newArguments = new ArrayList( arguments.size());
    for( Iterator iter = arguments(); iter.hasNext(); ) {
      Expression expr = (Expression)((Expression)iter.next()).visit( v);
      if( expr != null) {
        newArguments.add( expr);
      }
    }

    return reconstruct( Node.condVisit(this.ext, v), newPrimary, kind, newArguments);
  }

  MethodTypeInstance mti;

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    TypeSystem ts = c.getTypeSystem();

    ClassType ct = c.getCurrentClass();

    if (kind == SUPER) {
	ct = (ClassType) ct.getSuperType();
    }

    List args = new LinkedList();

    for (Iterator iter = arguments(); iter.hasNext(); ) {
	Expression e = (Expression) iter.next();
	args.add(e.getCheckedType());
    }

    mti = ts.getConstructor(ct, args, c);

    return this;
  }

  public MethodTypeInstance getMTI() {
    return mti;
  }

  public String toString() {
    String s = "";
    if( primary != null) {
      s += primary.toString();
      s += ".";
    } 

    s += (kind == THIS ? "this(" : "super(");

    for (Iterator iter = arguments(); iter.hasNext(); ) {
      s += iter.next();
      if (iter.hasNext()) {
        s += ", ";
      }
    }

    s += ");";

    return s;
  }

  public void translate_no_override( LocalContext c, CodeWriter w)
  {
    if( primary != null) {
      primary.translate( c, w);
      w.write( "." + (kind == THIS ? "this(" : "super("));
    } 
    else {
      w.write( (kind == THIS ? "this(" : "super("));
    }

    w.begin(0);
    for (Iterator iter = arguments(); iter.hasNext(); ) {
      ((Expression)iter.next()).translate( c, w);
      if (iter.hasNext()) {
        w.write(",");
	w.allowBreak(0);
      }
    }
    w.end();
    w.write( "); ");
  }
  
  public void dump( CodeWriter w)
  {
    w.write( " ( CONSTRUCTOR CALL STMT");
    dumpNodeInfo( w);
    w.write( (kind == THIS ? " < this > " : " < super > "));
    w.write( ")");
  }
}

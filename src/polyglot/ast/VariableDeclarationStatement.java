package jltools.ast;

import jltools.util.*;
import jltools.types.*;
import jltools.visit.AmbiguityRemover;
import jltools.visit.SignatureCleaner;
import java.util.*;
import java.lang.ref.WeakReference;

/** 
 * A <code>VariableDeclarationStatement</code> is an immutable representation 
 * of a variable declaration, which consists of a type, one or more variable
 * names, and possible initilization expressions.
 */
public class VariableDeclarationStatement extends Statement 
{
  /**
   * This class corresponds to the VariableDeclarator production in
   * the Java grammar. (Section 19.8.2)
   */
  public static final class Declarator extends Statement
  {
    public String name;
    public int additionalDimensions;    
    // will be null for uninitialized variable.
    public Expression initializer;
    // declaration statement that we are a part of; hold it in a weak referenence to 
    // avoid cycles in the ast, so that the gc can perform well
    public WeakReference wrVDS;

    public LocalInstance li;

    /**
     * Creates a new Declarator for a variable named <code>n</code>, with 
     * <code>dims</code> dimensions beyond those of the declarations's base
     * type.
     */
    public Declarator( Node ext, VariableDeclarationStatement vds, 
                       String n, int dims, Expression init) 
    {
      this.ext = ext;
      name = n;
      additionalDimensions = dims;
      initializer = init;
      wrVDS = new WeakReference(vds);
      this.li = null;
    }

    public Declarator( VariableDeclarationStatement vds, 
                       String n, int dims, Expression init) {
	this(null, vds, n, dims, init);
    }


    /**
     * Lazily reconstruct the Declarator
     */
    public Declarator reconstruct (Node ext, VariableDeclarationStatement vds, 
                                   String n, int dims, Expression init)
    {
      if ( ! n.equals ( name) || this.ext != ext ||
           dims != additionalDimensions ||
           init != initializer ||
           vds != (VariableDeclarationStatement)wrVDS.get() )
      {
        Declarator d = new Declarator ( ext, vds, n, dims, init);
        d.copyAnnotationsFrom ( this );
	d.li = li;
        return d;
      }
      return this;
    }

    public Declarator reconstruct (VariableDeclarationStatement vds, 
                                   String n, int dims, Expression init) {
	return reconstruct(this.ext, vds, n, dims, init);
    }

    public LocalInstance getLocalInstance() {
      return li;
    }
    
    public Node visitChildren( NodeVisitor v)
    {
        return reconstruct ( Node.condVisit(this.ext, v), (VariableDeclarationStatement)wrVDS.get(), name, 
                             additionalDimensions, 
                             (Expression)Node.condVisit(initializer, v) );
    }

    public Node removeAmbiguities( LocalContext c) throws SemanticException
    {
      /* Only add to context if inside a method, hence a local variable 
       * declaration. */
      if( c.inMethodScope() ) {
        VariableDeclarationStatement vdsEnclosing = 
          (VariableDeclarationStatement)wrVDS.get();
        LocalInstance li = c.getTypeSystem().newLocalInstance( name, 
                                              vdsEnclosing.typeForDeclarator(this),
                                              vdsEnclosing.accessFlags );
        /* If it is a constant numeric expression (final + initializer is 
         * IntLiteral) then mark it "constant" under FieldInstance. */
        // FIXME other literal types?
        if( initializer instanceof NumericalLiteral 
            && initializer != null && vdsEnclosing.accessFlags.isFinal()) {
          li.setConstantValue( new Long(
              ((NumericalLiteral)initializer).getValue())); 
        }
        c.addSymbol( name, li);
      }
      return this;
    }
      
    public Node typeCheck( LocalContext c) throws SemanticException
    {

      VariableDeclarationStatement vdsEnclosing = 
        (VariableDeclarationStatement)wrVDS.get();
      /* Only add to context if inside a method, hence a local variable 
       * declaration. */
      if( c.inMethodScope() ) {
        if (c.isDefinedLocally( name) )
          throw new SemanticException("Duplicate declaration of \"" + 
                                      name + "\"",
				      Annotate.getLineNumber(this));
          
        /* If it is a constant numeric expression (final + initializer is 
         * IntLiteral) then mark it "constant" under FieldInstance. */
        // FIXME other literal types?
        li = c.getTypeSystem().newLocalInstance( name, 
                                              vdsEnclosing.typeForDeclarator(this),
                                              vdsEnclosing.accessFlags );
        if( initializer instanceof NumericalLiteral 
            && initializer != null && vdsEnclosing.accessFlags.isFinal()) {
          li.setConstantValue( new Long(
                                 ((NumericalLiteral)initializer).getValue())); 
        }
        c.addSymbol( name, li);
      }
      
      if (initializer != null) {
        Type type = vdsEnclosing.typeForDeclarator( this );
          if( !c.getTypeSystem().isImplicitCastValid( 
                            initializer.getCheckedType(), 
                            type)) {
            throw new SemanticException( "The type of the variable initializer "
                           + "\"" 
                           + initializer.getCheckedType().getTypeString()
                           + "\" does not match that of the declaration \"" 
                           + type.getTypeString() + "\".",
			   Annotate.getLineNumber(this));
          }
      }
      return this;
    }

    public void translate( LocalContext c, CodeWriter w)
    {
      w.write ( name);
      for (int i = 0; i < additionalDimensions; i++)
        w.write("[]");
      if ( initializer != null)
      {
        w.write( " =");
	w.allowBreak(2, " ");
        initializer.translate(c, w);
      }
    }

    public void dump( CodeWriter w) throws SemanticException
    {
      VariableDeclarationStatement vdsEnclosing = 
        (VariableDeclarationStatement)wrVDS.get();
      w.write( "( < " + name + " > < " 
               + vdsEnclosing.typeForDeclarator( this).getTypeString() + " > ) ");
    }
  }


  protected final TypeNode tn; 
  /** A list of <code>Declarators</code>. */
  protected final List declarators; 
  protected final AccessFlags accessFlags;

  /**
   * Creates a new <code>VariableDeclarationStatement</code>.
   *
   * @pre Every element of <code>declarators</code> must be a 
   *  <code>Declarator</code>.
   */
  public VariableDeclarationStatement( Node ext, AccessFlags accessFlags,
                                       TypeNode tn, List declarators) {
    this.ext = ext;
    this.accessFlags = accessFlags;
    this.tn = tn;
    List l = new ArrayList( declarators.size());
    for (ListIterator i = declarators.listIterator(); i.hasNext(); )
    {
      Declarator d = (Declarator)i.next();
      l.add( d.reconstruct ( d.ext, this, d.name, d.additionalDimensions, d.initializer));
    }
    this.declarators = TypedList.copyAndCheck( l, Declarator.class,
                                               true);
  }

  public VariableDeclarationStatement( AccessFlags accessFlags,
                                       TypeNode tn, List declarators) {
      this(null, accessFlags, tn, declarators);
  }

  /**
   * Lazily reconstruct this node.
   */
  public VariableDeclarationStatement reconstruct( Node ext, AccessFlags accessFlags,
                                                   TypeNode tn,
                                                   List declarators) {
    if( !this.accessFlags.equals( accessFlags) || this.tn != tn || this.ext != ext
        || this.declarators.size() != declarators.size()) {
      VariableDeclarationStatement n 
        = new VariableDeclarationStatement( ext, accessFlags, tn, declarators);
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < declarators.size(); i++) {
        if( this.declarators.get( i) != declarators.get( i)) {
          VariableDeclarationStatement n 
            = new VariableDeclarationStatement( ext, accessFlags, tn, declarators);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      return this;
    }
  }

  public VariableDeclarationStatement reconstruct( AccessFlags accessFlags,
                                                   TypeNode tn,
                                                   List declarators) {
      return reconstruct(this.ext, accessFlags, tn, declarators);
  }

  /*
   * FIXME is this ever possible??
   *
   * Effects: Creates a new VariableDeclarationStatement of type <type>
   *   with no variables being declared.
  public VariableDeclarationStatement (Type baseType) {
    this.type = type;
    variables = new ArrayList();
  }
  */

  /**
   * Returns the accessFlags for this, or null for none.
   */
  public AccessFlags getAccessFlags() 
  {
    return accessFlags; //don't return a copy! (FieldNode depends on this)
  }

    public int numDeclarators() {
	return declarators.size();
    }

  /**
   * Yields all the declarators in this node, in order. 
   */
  public Iterator declarators() 
  {
    return declarators.iterator();
  }

  /** 
   * Returns the declarator at position <code>pos</code>.
   */ 
  public Declarator declaratorAt( int pos) 
  {
    return (Declarator)declarators.get( pos);
  }

  /**
   * FIXME is this method necessary?
   * 
   * Gets the type of this declaration statement.
  public TypeNode getTypeNode() {
    return type;
  }
  */

  /**
   * Returns the actual type (including any additional array dimensions) of 
   * the variable declared by <code>decl</code>.
   * 
   * @pre Requires that <code>decl</code> be a member of 
   *  <code>this.declarators</code>.
   */
  public Type typeForDeclarator(Declarator decl) throws SemanticException 
  {
    if (decl.additionalDimensions > 0) {
      return tn.getType().extendArrayDims( decl.additionalDimensions);
    }
    else {
      return tn.getType();
    }
  }
  
  /**
   * Visit the children of this node.
   *
   * @pre Requires that <code>tn.visit</code> returns an object of type
   *  <code>TypeNode</code> and that the <code>visit</code> method for each
   *  of the initializer expressions returns an object of type 
   *  <code>Expression</code>.
   */
  public Node visitChildren( NodeVisitor v)
  {
    TypeNode newTn = (TypeNode)tn.visit( v);
    List newDeclarators = new ArrayList( declarators.size());

    for( Iterator iter = declarators(); iter.hasNext(); ) {
      newDeclarators.add ( ((Declarator)iter.next()).visit ( v ) );
    }

    return reconstruct( Node.condVisit(this.ext, v), accessFlags, newTn, newDeclarators);
  }

  public Node cleanupSignatures( LocalContext c, SignatureCleaner sc) 
    throws SemanticException
  {
    TypeNode newTn = (TypeNode)tn.visit(sc);
    
    VariableDeclarationStatement vds = reconstruct ( ext, accessFlags, 
                                                     newTn, declarators);
    List newDeclarators = new ArrayList ( declarators.size());
    for (Iterator iter = vds.declarators(); iter.hasNext(); )
    {
      newDeclarators.add( ((Declarator)iter.next()).visit( sc ));
    }
    return vds.reconstruct ( accessFlags, newTn, newDeclarators);
  }

  public Node removeAmbiguities( LocalContext c, AmbiguityRemover ar) 
    throws SemanticException
  {
    TypeNode newTn = (TypeNode)tn.visit(ar);
    
    VariableDeclarationStatement vds = reconstruct ( ext, accessFlags, 
                                                     newTn, declarators);
    List newDeclarators = new ArrayList ( declarators.size());
    for (Iterator iter = vds.declarators(); iter.hasNext(); )
    {
      newDeclarators.add( ((Declarator)iter.next()).visit( ar ));
    }
    return vds.reconstruct ( accessFlags, newTn, newDeclarators);
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( accessFlags.getStringRepresentation());
    tn.translate( c, w);
    w.write(" ");
    for( Iterator iter = declarators(); iter.hasNext(); ) {
      Declarator decl = (Declarator)iter.next();
      decl.translate(c, w);

      if( iter.hasNext()) {
        w.write( ",");
	w.allowBreak(2, " ");
      }
    }
    w.write(";");
  }
  
  public void dump( CodeWriter w) throws SemanticException
  {
    w.write( "VAR DECL");
    w.write( " < " + accessFlags.getStringRepresentation() + "> ");
    dumpNodeInfo( w);
    
  }
  
  public TypeNode getTypeNode() { return tn; }
}

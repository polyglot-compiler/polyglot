package jltools.ast;

import jltools.util.*;
import jltools.types.*;

import java.util.*;


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
  public static final class Declarator 
  {
    public String name;
    public int additionalDimensions;    
    // will be null for uninitialized variable.
    public Expression initializer;
    /**
     * Creates a new Declarator for a variable named <code>n</code>, with 
     * <code>dims</code> dimensions beyond those of the declarations's base
     * type.
     */
    public Declarator( String n, int dims, Expression init) 
    {
      name = n;
      additionalDimensions = dims;
      initializer = init;
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
  public VariableDeclarationStatement( AccessFlags accessFlags,
                                       TypeNode tn, List declarators) {
    this.accessFlags = accessFlags;
    this.tn = tn;
    this.declarators = TypedList.copyAndCheck( declarators, Declarator.class,
                                               true);
  }

  /**
   * Lazily reconstruct this node.
   */
  public VariableDeclarationStatement reconstruct( AccessFlags accessFlags,
                                                   TypeNode tn,
                                                   List declarators) {
    if( !this.accessFlags.equals( accessFlags) || this.tn != tn
        || this.declarators.size() != declarators.size()) {
      VariableDeclarationStatement n 
        = new VariableDeclarationStatement( accessFlags, tn, declarators);
      n.copyAnnotationsFrom( this);
      return n;
    }
    else {
      for( int i = 0; i < declarators.size(); i++) {
        if( this.declarators.get( i) != declarators.get( i)) {
          VariableDeclarationStatement n 
            = new VariableDeclarationStatement( accessFlags, tn, declarators);
          n.copyAnnotationsFrom( this);
          return n;
        }
      }
      return this;
    }
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
    return accessFlags;
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
  Node visitChildren( NodeVisitor v)
  {
    TypeNode newTn = (TypeNode)tn.visit( v);

    List newDeclarators = new ArrayList( declarators.size());

    for( Iterator iter = declarators(); iter.hasNext(); ) {
      Declarator decl = (Declarator)iter.next();
      if( decl.initializer != null) {
        Expression newInitializer = (Expression)decl.initializer.visit( v);
        if( newInitializer != decl.initializer) {
          newDeclarators.add( new Declarator( decl.name,
                                              decl.additionalDimensions,
                                              newInitializer));
        }
        else {
          newDeclarators.add( decl);
        }
      }
      else {
        newDeclarators.add( decl);
      }   
    }

    return reconstruct( accessFlags, newTn, newDeclarators);
  }

  public Node removeAmbiguities( LocalContext c) throws SemanticException
  {
    /* Only add to context if inside a method, hence a local variable 
     * declaration. */
    if( c.getCurrentMethod() != null) {
      for( Iterator iter = declarators(); iter.hasNext(); ) {
        Declarator decl = (Declarator)iter.next();
        /* If it is a constant numeric expression (final + initializer is 
         * IntLiteral) then mark it "constant" under FieldInstance. */
        // FIXME other literal types?
        FieldInstance fi = new FieldInstance( decl.name, 
                                              typeForDeclarator(decl), 
                                              null, accessFlags);
        if( decl.initializer instanceof IntLiteral 
            && decl.initializer != null && accessFlags.isFinal()) {
          fi.setConstantValue( new Long(
              ((IntLiteral)decl.initializer).getLongValue())); 
        }
        c.addSymbol( decl.name, fi);
      }
    }
    
    return this;
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    /* Only add to context if inside a method, hence a local variable 
     * declaration. */
    if( c.getCurrentMethod() != null) {
      for( Iterator iter = declarators(); iter.hasNext(); ) {
        Declarator decl = (Declarator)iter.next();

        if (c.isDefinedLocally( decl.name) )
          throw new SemanticException("Duplicate declaration of \"" + 
                                      decl.name + "\"");

        /* If it is a constant numeric expression (final + initializer is 
         * IntLiteral) then mark it "constant" under FieldInstance. */
        // FIXME other literal types?
        FieldInstance fi = new FieldInstance( decl.name, 
                                              typeForDeclarator(decl), 
                                              null, accessFlags);
        if( decl.initializer instanceof IntLiteral 
            && decl.initializer != null && accessFlags.isFinal()) {
          fi.setConstantValue( new Long(
              ((IntLiteral)decl.initializer).getLongValue())); 
        }
        c.addSymbol( decl.name, fi);
      }
    }
     
    for( Iterator iter = declarators(); iter.hasNext() ;) {
      Declarator decl = (Declarator)iter.next();
      if (decl.initializer != null) {
        Type type = typeForDeclarator( decl);

        if( !c.getTypeSystem().isImplicitCastValid( 
                                  decl.initializer.getCheckedType(), 
                                  type)) {
          throw new SemanticException( "The type of the variable initializer "
                           + "\"" 
                           + decl.initializer.getCheckedType().getTypeString()
                           + "\" does not match " 
                           + "that of the declaration \"" 
                           + type.getTypeString() + "\".");
        }
      }
    }

    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( accessFlags.getStringRepresentation());
    tn.translate( c, w);
    w.write(" ");
    for( Iterator iter = declarators(); iter.hasNext(); ) {
      Declarator decl = (Declarator)iter.next();
      if( decl.initializer != null) {
        w.write( decl.name);
        for (int i = 0; i < decl.additionalDimensions; i++) {
          w.write( "[]");
        }
        w.write( " =");
	w.allowBreak(2, " ");
        decl.initializer.translate( c, w);
      }
      else {
        w.write( decl.name);
        for( int i = 0; i < decl.additionalDimensions; i++) {
          w.write( "[]");
        }
      }
      if( iter.hasNext()) {
        w.write( ",");
	w.allowBreak(2, " ");
      }
    }
    w.write(";");
  }
  
  public void dump( CodeWriter w) throws SemanticException
  {
    w.write( "( VAR DECL");
    w.write( " < " + accessFlags.getStringRepresentation() + "> ");
    dumpNodeInfo( w);
    w.write(")");
    
    w.begin(0);
    
    for( Iterator iter = declarators(); iter.hasNext(); ) {
      Declarator decl = (Declarator)iter.next();
      if (decl.initializer != null) {
        w.write( "( < " + decl.name + " > < " 
                 + typeForDeclarator( decl).getTypeString() + " > ) ");
        decl.initializer.dump( w);
      }
      else {
        w.write( "( < " + decl.name + " > < " 
                 + typeForDeclarator( decl).getTypeString() + " > ) ");
      }
      if( iter.hasNext()) {
        w.allowBreak(0, " ");
      }
    }
    
    w.end();
  }
}

/*
 * VariableDeclarationStatement.java
 */ 

package jltools.ast;

import jltools.util.TypedListIterator;
import jltools.util.TypedList;
import jltools.util.CodeWriter;
import jltools.types.Context;
import jltools.types.Type;
import jltools.types.AccessFlags;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;

/** 
 * VariableDeclarationStatement
 *
 * Overview: A VariableDeclarationStatements is a mutable representation of
 *   a variable declaration, which consists of a type, one or more variable
 *   names, and possible initilization expressions.
 */
public class VariableDeclarationStatement extends Statement {
  /**
   * This class corresponds to the VariableDeclarator production in
   * the Java grammar. (Section 19.8.2)
   **/
  public static final class Declarator {
    public String name;
    public final int additionalDims;    
    // will be null for uninitialized variable.
    public final Expression initializer;
    /**
     * Creates a new Declarator for a variable named <n>, with <dims>
     *   dimensions beyond those of the declarations's base type.
     **/
    public Declarator(String n, int dims, Expression init) {
      name = n;
      additionalDims = dims;
      initializer = init;
    }
  }
  /**
   * Requires: Every element of <declList> is a Declarator.
   * Effects: Creates a new VariableDeclarationStatement of type <type>,
   *    with optional modifiers <optModifiers>, and declarations <declList>.
   **/
  public VariableDeclarationStatement(AccessFlags optModifiers,
				      TypeNode type, List declList) {
    modifiers = optModifiers;
    this.type = type;
    TypedList.check(declList, Declarator.class);
    variables = new ArrayList(declList);
  }     

  /**
   * Requires: Every element of <declList> is a Declarator.
   * Effects: Creates a new VariableDeclarationStatement of type <type>,
   *    with optional modifiers <optModifiers>, and declarations <declList>.
   **/
  public VariableDeclarationStatement(AccessFlags optModifiers,
				      Type type, List declList) {
    this (optModifiers, new TypeNode(type), declList);
  }     


  /**
   * Effects: Creates a new VariableDeclarationStatement of type <type>
   *   with no variables being declared.
   */
  public VariableDeclarationStatement (Type baseType) {
    this.type = type;
    variables = new ArrayList();
  }

  /**
   * Adds a new variable named <var> with initializer <init> to
   * this VariableDelarationStatement.
   */
  public void addVariable(Declarator decl) {
    variables.add(decl);
  }

  /** 
   * Returns the variable name at position <pos>.  Throws an
   * IndexOutOfboundsException if <pos> is not a valid position.
   **/ 
  public Declarator variableAt(int pos) {
    return (Declarator) variables.get(pos);
  }
  
  /**
   * Removes the variable at position <pos> in the variable declaration.
   * Throws an IndexOutOfBoundsException if <pos> is not a valid
   * position.
   **/ 
  public void removeVariable(int pos) {
    variables.remove(pos);
  }

  /**
   * Gets the type of this declaration statement.
   **/
  public TypeNode getType() {
    return type;
  }

  /**
   * Sets the type of this declaration statement.
   **/
  public void setType(TypeNode type) {
    this.type = type;
  }

  /**
   * Sets the type of this declaration statement.
   **/
  public void setType(Type type) {
    this.type = new TypeNode(type);
  }

  /**
   * Requires: decl is a declarator in this.
   *
   * Effects: returns the actual type of the variable declared by <decl>.
   **/
  public Type typeForDeclarator(Declarator decl) {
    if (decl.additionalDims > 0)
      return type.getType().extendArrayDims(decl.additionalDims);
    else
      return type.getType();
  }

  /**
   * Returns the modifiers for this, or null for none.
   **/
  public AccessFlags getModifiers() {
    return modifiers;
  }

  /**
   * Sets the modifiers for this to be <modifiers>
   **/
  public AccessFlags setModifiers() {
    return modifiers;
  }
  
   /**
    *
    */
   void visitChildren(NodeVisitor vis)
   {
    type = (TypeNode) type.visit(vis);
    ListIterator it = variables.listIterator();
    while (it.hasNext()) {
      Declarator pair = (Declarator)it.next();
      if (pair.initializer != null)
      {
         Expression newExpr = (Expression) pair.initializer.visit(vis);
         if (newExpr != pair.initializer)
            it.set(new Declarator(pair.name, pair.additionalDims, newExpr));
      }
    }
   }

   public Node typeCheck(Context c)
   {
      // FIXME: implement
      return this;
   }

   public void translate(Context c, CodeWriter w)
   {
      w.write( modifiers.getStringRepresentation());
      type.translate(c, w);
      ListIterator it = variables.listIterator();
      while (it.hasNext())
      {
         Declarator pair = (Declarator)it.next();
         if (pair.initializer != null)
         {
            w.write(pair.name + "= ");
            pair.initializer.translate(c, w);
         }
         else
            w.write(pair.name);
         if (it.hasNext())
            w.write(", ");
      }
      w.write(";");

   }

   public void dump(Context c, CodeWriter w)
   {
      w.write( "(" + modifiers.getStringRepresentation());
      type.translate(c, w);
      ListIterator it = variables.listIterator();
      while (it.hasNext())
      {
         Declarator pair = (Declarator)it.next();
         if (pair.initializer != null)
         {
            w.write("(" + pair.name + ", ");
            pair.initializer.translate(c, w);
         }
         else
            w.write(pair.name + ", Nil");
         if (it.hasNext())
            w.write("), ");
      }
      w.write(";");
   }

  /**
   * Yields all the declarators in this, in order. 
   **/
  public Iterator declarators() {
    return new TypedListIterator(variables.listIterator(), 
				 Declarator.class, false);
  }

  public Node copy() {
    return copy(false);
  }
  public Node deepCopy() {
    return copy(true);
  }
  
  private Node copy(boolean deep) {
    AccessFlags mods = modifiers == null ? null : modifiers.copy();
    ArrayList list = new ArrayList(variables.size());
    for (Iterator i = variables.iterator(); i.hasNext(); ) {
      Declarator d = (Declarator) i.next();
      Expression expr = d.initializer == null ? null :
	(deep ? (Expression) d.initializer.deepCopy() : d.initializer);
      list.add(new Declarator(d.name, d.additionalDims, expr));
    }
    TypeNode tn = (TypeNode) type.copy();
    VariableDeclarationStatement vds = 
      new VariableDeclarationStatement(mods, type, list);
    vds.copyAnnotationsFrom(this);
    return vds;
  }
  
  private TypeNode type; 
  // RI: every member is a Declarator
  private List variables; 
  private AccessFlags modifiers;
}

    

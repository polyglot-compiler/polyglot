/*
 * VariableDeclarationStatement.java
 */ 

package jltools.ast;

import jltools.util.TypedListIterator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;

/** 
 * VariableDeclarationStatement
 *
 * Overview: A VariableDeclarationStatements is a mutable representation of
 *   a variable declaration, which consists of a type, one or more variable
 *   names, and possible initilization expressions.
 */
public class VariableDeclarationStatement extends Statement {
  private static final class VariableDeclarationPair {
    String name;
    Expression initializer; // will be null for unitilized variable
    public VariableDeclarationPari(String n, Expression i) {
      name = n;
      initializer = i;
    }
  }
  /**
   * Effects: Creates a new VariableDeclarationStatement of type <type>
   *   with no variables being declared.
   */
  public VariableDeclarationStatement (Type type) {
    this.type = type;
  }

  /**
   * Adds a new variable named <var> with initializer <init> to
   * this VariableDelarationStatement.
   */
  public void addVariable(String var, Expression init) {
    variables.add(new VariableDeclarationPair(var, init));
  }

  /** 
   * Returns the variable name at position <pos>.  Throws an
   * IndexOutOfboundsException if <pos> is not a valid position.
   */ 
  public String variableNameAt(int pos) {
    return ((VariableDeclarationPair) variables.get(pos)).name;
  }

  /** 
   * Returns the initializer at position <pos>.  If the variable is
   * not explicitly initialized, returns null.  Throws an
   * IndexOutOfboundsException if <pos> is not a valid position.
   */ 
  public Expression initializerAt(int pos) {
    return ((VariableDeclarationPair) variables.get(pos)).initializer;
  }
  
  /**
   * Removes the variable at position <pos> in the variable declaration.
   * Throws an IndexOutOfBoundsException if <pos> is not a valid
   * position.
   */ 
  public void removeVariable(int pos) {
    variables.remove(pos);
  }

  public Node accept(NodeVisitor v) {
    return v.visitVariableDeclarationStatement(this);
  }

  /**
   * Requires: v will not transform an Expression into anything other than
   *   another Expression or null.
   */
  public void visitChildren(NodeVisitor v) {
    ListIterator it = variables.listIterator();
    while (it.hasNext()) {
      VariableDeclarationPair pair = (VariableDeclarationPair)it.next();
      pair.initilizer = (Expression) pair.initilizer.accept(v);
    }
  }

  
  
  private Type type; 
  // RI: every member is a VariableDeclarationPair
  private ArrayList variables; 

}

    

/*
 * LocalVariableExpression
 */

package jltools.ast;

/** 
 * LocalVariableExpression
 * 
 * Overview: A LocalVariableExpression corresponds to a mutalbe reference to
 *   a local variable (not a field of a class) in an expression.
 */

public class LocalVariableExpression extends Expression {
    
    /** 
     * Effects: Creates a new local variable reference to a
     * variable named <name>.
     */
    public LocalVariableExpression(String name) {
	this.name = name;
    }

    /** 
     * Effects: Returns the name of the variable referenced by this
     */
    public String getName() {
	return name;
    }

    /** 
     * Effects: Change the name of the variable referenced by this
     *    to <newName>.
     */
    public void setName(String newName) {
	name = newName;
    }

    public Node accept(NodeVisitor v) {
	return v.visitLocalVariableExpression(this);
    }

    public void visitChildren(NodeVisitor v) {
    }

    public Node copy() {
      LocalVariableExpression lve = new LocalVariableExpression(name);
      lve.copyAnnotationsFrom(this);
      return lve;
    }

    public Node deepCopy() {
      return copy();
    }

    private String name;
}

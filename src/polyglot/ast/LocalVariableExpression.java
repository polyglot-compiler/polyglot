/*
 * LocalVariableExpression
 */

package jltools.ast;
import jltools.types.LocalContext;
import jltools.util.CodeWriter;

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

  public void translate(LocalContext c, CodeWriter w)
  {
    w.write(name);
  }

  public void dump(LocalContext c, CodeWriter w)
  {
    w.write(" ( " + name );
    dumpNodeInfo(c, w);
    w.write(" ) ");
  }

  public Node typeCheck(LocalContext c)
  {
    // FIXME; implement
    return this;
  }

    public void visitChildren(NodeVisitor v) {
      // nothing to do
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

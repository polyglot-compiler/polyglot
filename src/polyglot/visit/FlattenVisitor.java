package jltools.visit;

import jltools.ast.*;
import jltools.types.*;

import java.util.*;

/**
 * The FlattenVisitor flattens the AST,
 */
public class FlattenVisitor extends NodeVisitor
{
  LinkedList stack;
  ExtensionFactory ef;
  TypeSystem ts;

  public FlattenVisitor(TypeSystem ts, ExtensionFactory ef) {
      this.ts = ts;
      this.ef = ef;
      stack = new LinkedList();
  }

  public Node override (Node n) {
      // do not enter FieldNodes 
      if (n instanceof FieldNode) {
          return n;
      }
      return null;
  }

  static int count = 0;

  private static String newID() {
      return "flat$$$" + count++;
  }

  /** 
   * When entering a BlockStatement, place a new StatementList
   * onto the stack
   */
  public NodeVisitor enter( Node n )
  {
    if (n instanceof BlockStatement) {
	stack.addFirst(new LinkedList());
    }

    return this;
  }

  /** 
   * Flatten complex expressions within the AST
   */
  public Node leave( Node old, Node n, NodeVisitor v)
  {
    if (n instanceof BlockStatement) {
	List l = (List) stack.removeFirst();
	return new BlockStatement(l);
    }
    else if (n instanceof Statement) {
	List l = (List) stack.getFirst();
	l.add(n);
	return n;
    }
    else if (n instanceof Expression &&
	  ! (n instanceof Literal) &&
	  ! (n instanceof ExpressionStatement) &&
	  ! (n instanceof LocalVariableExpression)) {

	Expression e = (Expression) n;

	// create a local temp, initialized to the value of the complex
	// expression

	String name = newID();
        VariableDeclarationStatement.Declarator temp =
	    new VariableDeclarationStatement.Declarator(
	    ef.getNewVariableDeclaratorExtension(), null, name, 0, e);

	LocalInstance li = ts.newLocalInstance(name,
	    e.getCheckedType(), new AccessFlags());
	temp.setVariableInstance(li);

	List decls = new LinkedList();
	decls.add(temp);

	VariableDeclarationStatement s =
	    new VariableDeclarationStatement(new AccessFlags(),
	    new TypeNode(ef.getNewTypeNodeExtension(),
	    e.getCheckedType()), decls);
	
	List l = (List) stack.getFirst();
	l.add(s);

        // return the local temp instead of the complex expression
	LocalVariableExpression lve = new LocalVariableExpression(
	    ef.getNewLocalVariableExpressionExtension(),
	    name);
	lve.setLocalInstance(li);
	lve.setCheckedType(e.getCheckedType());
	return lve;
    }

    return n;
  }
}

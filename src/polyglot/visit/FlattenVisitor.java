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
      if (n instanceof FieldNode || n instanceof ConstructorCallStatement) {
          return n;
      }

      return null;
  }

  static int count = 0;

  private static String newID() {
      return "flat$$$" + count++;
  }

  Node noFlatten = null;

  /** 
   * When entering a BlockStatement, place a new StatementList
   * onto the stack
   */
  public NodeVisitor enter( Node n )
  {
    if (n instanceof BlockStatement) {
	stack.addFirst(new LinkedList());
    }

    if (n instanceof ExpressionStatement) {
	// Don't flatten the expression contained in the statement, but
	// flatten its subexpressions.

	ExpressionStatement s = (ExpressionStatement) n;

	// Can't do this since visitChildren is protected.
	// s.getExpression().visitChildren(this);

	noFlatten = s.getExpression();
    }

    return this;
  }

  /** 
   * Flatten complex expressions within the AST
   */
  public Node leave( Node old, Node n, NodeVisitor v)
  {
    if (n == noFlatten) {
	noFlatten = null;
	return n;
    }

    if (n instanceof BlockStatement) {
	List l = (List) stack.removeFirst();
	return new BlockStatement(ef.getNewBlockStatementExtension(), l);
    }
    else if (n instanceof Statement &&
	! (n instanceof VariableDeclarationStatement.Declarator)) {
	List l = (List) stack.getFirst();
	l.add(n);
	return n;
    }
    else if (n instanceof Expression &&
	  ! (n instanceof Literal) &&
	  ! (n instanceof SpecialExpression) &&
	  ! (n instanceof LocalVariableExpression)) {

	Expression e = (Expression) n;

	if (e instanceof BinaryExpression) {
	    BinaryExpression b = (BinaryExpression) e;
	    if (b.isAssignment()) {
		return n;
	    }
	}

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

	VariableDeclarationStatement s = new VariableDeclarationStatement(
	    ef.getNewVariableDeclarationStatementExtension(),
	    new AccessFlags(),
	    new TypeNode(ef.getNewTypeNodeExtension(), e.getCheckedType()),
	    decls);
	
	List l = (List) stack.getFirst();
	l.add(s);

        // return the local temp instead of the complex expression
	LocalVariableExpression lve = new LocalVariableExpression(
	    ef.getNewLocalVariableExpressionExtension(), name);
	lve.setLocalInstance(li);
	lve.setCheckedType(e.getCheckedType());
	return lve;
    }

    return n;
  }
}

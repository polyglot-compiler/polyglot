package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;
import java.util.*;

/**
 * An <code>Assign</code> represents a Java assignment expression.
 */
public class Assign_c extends Expr_c implements Assign
{
  protected Expr left;
  protected Operator op;
  protected Expr right;

  public Assign_c(JL del, Ext ext, Position pos, Expr left, Operator op, Expr right) {
    super(del, ext, pos);
    this.left = left;
    this.op = op;
    this.right = right;
  }

  /** Get the precedence of the expression. */
  public Precedence precedence() {
    return Precedence.ASSIGN;
  }

  /** Get the left operand of the expression. */
  public Expr left() {
    return this.left;
  }

  /** Set the left operand of the expression. */
  public Assign left(Expr left) {
    Assign_c n = (Assign_c) copy();
    n.left = left;
    return n;
  }

  /** Get the operator of the expression. */
  public Operator operator() {
    return this.op;
  }

  /** Set the operator of the expression. */
  public Assign operator(Operator op) {
    Assign_c n = (Assign_c) copy();
    n.op = op;
    return n;
  }

  /** Get the right operand of the expression. */
  public Expr right() {
    return this.right;
  }

  /** Set the right operand of the expression. */
  public Assign right(Expr right) {
    Assign_c n = (Assign_c) copy();
    n.right = right;
    return n;
  }

  /** Reconstruct the expression. */
  protected Assign_c reconstruct(Expr left, Expr right) {
    if (left != this.left || right != this.right) {
      Assign_c n = (Assign_c) copy();
      n.left = left;
      n.right = right;
      return n;
    }

    return this;
  }

  /** Visit the children of the expression. */
  public Node visitChildren(NodeVisitor v) {
    Expr left = (Expr) visitChild(this.left, v);
    Expr right = (Expr) visitChild(this.right, v);
    return reconstruct(left, right);
  }


  /** Type check the expression. */
  public Node typeCheck(TypeChecker tc) throws SemanticException {
    Type t = left.type();
    Type s = right.type();

    TypeSystem ts = tc.typeSystem();

    boolean intConversion = false;

    if (right instanceof NumLit) {
      long value = ((NumLit) right).longValue();
      intConversion = ts.numericConversionValid(t, value);
    }

    if (op == ASSIGN) {
      if (! ts.isImplicitCastValid(s, t) &&
          ! ts.equals(s, t) &&
          ! intConversion) {

        throw new SemanticException("Cannot assign " + s + " to " + t + ".",
                                    position());
      }

      return type(t);
    }

    if (op == ADD_ASSIGN) {
      if (ts.equals(t, ts.String()) || ts.equals(s, ts.String())) {
        return type(ts.String());
      }
    }

    if (op == BIT_AND_ASSIGN || op == BIT_OR_ASSIGN ||
        op == BIT_XOR_ASSIGN) {
      if (t.isBoolean() && s.isBoolean()) {
        return type(ts.Boolean());
      }
    }

    if (! t.isNumeric() || ! s.isNumeric()) {
      if (op == ADD_ASSIGN) {
        throw new SemanticException("The " + op + " operator must have "
                                    + "numeric or String operands.",
                                    position());
      }

      if (op == BIT_AND_ASSIGN || op == BIT_OR_ASSIGN ||
          op == BIT_XOR_ASSIGN) {
        throw new SemanticException("The " + op + " operator must have "
                                    + "numeric or boolean operands.",
                                    position());
      }

      if (op == SUB_ASSIGN || op == MUL_ASSIGN ||
          op == DIV_ASSIGN || op == MOD_ASSIGN ||
          op == SHL_ASSIGN || op == SHR_ASSIGN ||
          op == USHR_ASSIGN) {
        throw new SemanticException("The " + op + " operator must have "
                                    + "numeric operands.",
                                    position());
      }

      throw new InternalCompilerError("Unrecognized assignment operator " +
                                      op + ".");
    }

    if (op == SHL_ASSIGN || op == SHR_ASSIGN || op == USHR_ASSIGN) {
      // Only promote the left of a shift.
      return type(ts.promote(t));
    }
      
    return type(ts.promote(t, s));
  }
  
  public Type childExpectedType(Expr child, AscriptionVisitor av) {
      if (child == right) {
          return left.type();
      }

      return child.type();
  }

  /** Get the throwsArrayStoreException of the expression. */
  public boolean throwsArrayStoreException() {
    return op == ASSIGN &&
      left.type().isReference() &&
      left instanceof ArrayAccess;
  }

  /** Get the throwsArithmeticException of the expression. */
  public boolean throwsArithmeticException() {
    // conservatively assume that any division or mod may throw
    // ArithmeticException this is NOT true-- floats and doubles don't
    // throw any exceptions ever...
    return op == DIV_ASSIGN || op == MOD_ASSIGN;
  }

  public String toString() {
    return left + " " + op + " " + right;
  }

  /** Write the expression to an output file. */
  public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
    printSubExpr(left, true, w, tr);
    w.write(" ");
    w.write(op.toString());
    w.allowBreak(2, " ");
    printSubExpr(right, false, w, tr);
  }

  /** Dumps the AST. */
  public void dump(CodeWriter w) {
    super.dump(w);
    w.allowBreak(4, " ");
    w.begin(0);
    w.write("(operator " + op + ")");
    w.end();
  }

  public Term entry() {
    return ((LHS) left).lhsEntry(this);
  }

  public List acceptCFG(CFGBuilder v, List succs) {
    ((LHS) left).visitAssignCFG(this, v);
    return succs;
  }

  public List throwTypes(TypeSystem ts) {
    List l = new LinkedList();

    if (throwsArithmeticException()) {
      l.add(ts.ArithmeticException());
    }

    if (throwsArrayStoreException()) {
      l.add(ts.ArrayStoreException());
    }

    return l;
  }
}

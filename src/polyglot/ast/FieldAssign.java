package polyglot.ast;

/**
 * A <code>FieldAssign</code> represents a Java assignment expression to
 * a field, e.g. this.x = foo, where x is a field
 * 
 * The class of the Expr returned by FieldAssign.left() is guaranteed
 * to be a Field
 */
public interface FieldAssign extends Assign
{
}

package polyglot.ast;

/**
 * A <code>LocalAssign</code> represents a Java assignment expression to
 * a local variable, e.g. x = foo, where x is a local variable
 * 
 * The class of the Expr returned by LocalAssign.left() is guaranteed
 * to be a Local
 */
public interface LocalAssign extends Assign
{
}

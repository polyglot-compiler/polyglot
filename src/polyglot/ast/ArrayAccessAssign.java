package polyglot.ast;

/**
 * An <code>ArrayAccessAssign</code> represents a Java assignment expression to
 * an array element, e.g. A[3] = foo.
 * 
 * The class of the Expr returned by ArrayAccessAssign.left() is guaranteed
 * to be an ArrayAccess
 */
public interface ArrayAccessAssign extends Assign
{
    boolean throwsArrayStoreException();
}

package jltools.ast;

/**
 * An <code>ArrayAccess</code> is an immutable representation of an
 * access of an array member.
 */
public interface ArrayAccess extends Expr 
{
    Expr array();
    ArrayAccess array(Expr array);

    Expr index();
    ArrayAccess index(Expr index);
}

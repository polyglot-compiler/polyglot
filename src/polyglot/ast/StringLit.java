package jltools.ast;

/** 
 * A <code>StringLit</code> represents an immutable instance of a 
 * <code>String</code> which corresponds to a literal string in Java code.
 */
public interface StringLit extends Lit 
{
    String value();
    StringLit value(String value);
}

package jltools.ast;

/** 
 * An <code>CharLit</code> represents a literal in java of
 * <code>char</code> type.
 */
public interface CharLit extends NumLit
{    
    char value();
    CharLit value(char value);
}

package jltools.ast;

/**
 * An integer literal: chars or ints.
 */
public interface NumLit extends Lit
{
    long longValue();
}

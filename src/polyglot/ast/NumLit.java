package jltools.ast;

/**
 * An integer literal: longs, ints, shorts, bytes, and chars.
 */
public interface NumLit extends Lit
{
    long longValue();
}

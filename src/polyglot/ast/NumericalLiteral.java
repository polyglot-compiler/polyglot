package jltools.ast;

/**
 * class for the abstraction of chars and ints, longs, bytes and short literals.
 */
abstract class NumericalLiteral extends Literal
{
  abstract public long getValue();
}

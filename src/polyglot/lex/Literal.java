package jltools.lex;

import jltools.util.Position;

public abstract class Literal extends Token
{
  public Literal(Position position, int sym) { super(position, sym); }
}

package jltools.lex;

import java_cup.runtime.Symbol;

public class Identifier extends Token {
  String identifier;
  public Identifier(int line, String identifier)
  {
	super(line);
	this.identifier=identifier;
  }

  public String getIdentifier() { return identifier; }

  public String toString() { return "Identifier <"+identifier+">"; }

  /* Ben Walter <bwalter@mit.edu> correctly pointed out that
   * the first released version of this grammar/lexer did not
   * return the string value of the identifier in the parser token.
   * Should be fixed now. ;-) <cananian@alumni.princeton.edu>
   */
  public Symbol symbol() { return new Symbol(jltools.parse.sym.IDENTIFIER, this); }
}

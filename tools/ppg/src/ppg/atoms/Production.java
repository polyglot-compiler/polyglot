package jltools.util.jlgen.atoms;

import java.util.*;
import jltools.util.jlgen.parse.*;
import jltools.util.jlgen.util.*;
public class Production implements Unparse
{
	private Nonterminal lhs;
	private Vector rhs;
	
	public Production(Nonterminal lhs, Vector rhs)
	{
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public Nonterminal getLHS() { return lhs; }	public Vector getRHS() { return rhs; }
	
	public void unparse (CodeWriter cw) {		cw.begin(0);
		cw.write(lhs.toString() + " ::=");
		cw.allowBreak(3);		Vector rhs_part;
		for (int i=0; i < rhs.size(); i++) {
			rhs_part = (Vector) rhs.elementAt(i);			for (int j=0; j < rhs_part.size(); j++) {
				cw.write(" ");
				((GrammarPart) rhs_part.elementAt(j)).unparse(cw);			}			if (i < rhs.size() - 1) {				cw.allowBreak(0);
				cw.write(" | ");			}					}		cw.write(";");
		cw.newline(); cw.newline();	
		cw.end();	}
		public String toString() {		String result = lhs.toString();		Vector rhs_part;
		result += " ::=";
		for (int i=0; i < rhs.size(); i++) {
			rhs_part = (Vector) rhs.elementAt(i);			for (int j=0; j < rhs_part.size(); j++) {
				result += " " + rhs_part.elementAt(j).toString();			}			if (i < rhs.size() - 1)				result += " | ";
		}		return result + ";";	}
}

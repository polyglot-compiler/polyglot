package jltools.util.jlgen.atoms;

import java.util.*;

public class Production 
{
	private Nonterminal lhs;
	private Vector rhs;
	
	public Production(Nonterminal lhs, Vector rhs)
	{
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public Nonterminal getLHS() { return lhs; }	public Vector getRHS() { return rhs; }
		public String toString() {		String result = lhs.toString();		Vector rhs_part;
		result += " ::=";
		for (int i=0; i < rhs.size(); i++) {
			rhs_part = (Vector) rhs.elementAt(i);			for (int j=0; j < rhs_part.size(); j++) {
				result += " " + rhs_part.elementAt(j).toString();			}			if (i < rhs.size() - 1)				result += " | ";
		}		return result + ";";	}
}

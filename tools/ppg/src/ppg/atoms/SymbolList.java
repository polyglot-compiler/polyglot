package jltools.util.jlgen.atoms;

import java.util.*;

public class SymbolList
{
	public static final int TERMINAL = 0;
	public static final int NONTERMINAL = 1;
	
	private int variety;
	private String type;
	private Vector symbols;
	
	public SymbolList(int which, String type, Vector syms) {
		variety = which;
		this.type = type;
		symbols = syms;
	}	
	
	public String toString() {
		String result = "";

		switch (variety) {
			case (TERMINAL): result = "terminal "; break;
			case (NONTERMINAL): result = "non terminal "; break;
		}
		
		if (type != null)
			result += type + " ";
		
		int size = symbols.size();
		for (int i=0; i < size; i++) {
			result += (String)symbols.elementAt(i);
			if (i < size - 1)
				result += ", ";
		}
		return result + ";";
	}
}

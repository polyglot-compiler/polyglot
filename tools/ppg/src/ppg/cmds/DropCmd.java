package jltools.util.jlgen.cmds;

import java.util.*;
import jltools.util.jlgen.atoms.*;
import jltools.util.jlgen.util.*;
public class DropCmd implements Command
{
	private Production prod; // productions to be dropped for some nonterminal
	private String sym; // or, the single nonterminal to be dropped
	
	public DropCmd(String symbol)
	{		// here it does not matter whether it's a nonterminal or terminal
		sym = symbol;
		prod = null;
	}
		public DropCmd(Production productions)
	{
		prod = productions;
		sym = null;
	}

	public boolean isProdDrop() { return prod != null; }
	public boolean isSymbolDrop() { return sym != null; }		public Production getProduction() { return prod; }
	public String getSymbol() { return sym; }	
	public void unparse(CodeWriter cw) {
		//cw.begin(0);		cw.write("DropCmd");		cw.allowBreak(0);		if (prod != null)			prod.unparse(cw);		else			cw.write(sym);
		//cw.end();
	}	
}
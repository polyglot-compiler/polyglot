package jltools.util.jlgen.cmds;

import jltools.util.jlgen.atoms.*;import jltools.util.jlgen.util.*;

public class NewProdCmd implements Command
{
	private Production prod;
	
	public NewProdCmd(Production p) 
	{
		prod = p;
	}

	public Production getProduction() { return prod; }
		public void unparse(CodeWriter cw) {
		cw.begin(3);		cw.write("NewProdCmd");		cw.allowBreak(2);		cw.end();
	}
}
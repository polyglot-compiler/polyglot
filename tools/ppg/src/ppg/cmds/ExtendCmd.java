package jltools.util.jlgen.cmds;

import jltools.util.jlgen.atoms.*;import jltools.util.jlgen.util.*;

public class ExtendCmd implements Command
{
	private Production prod;
	
	public ExtendCmd(Production p)
	{
		prod = p;
	}	
	public Production getProduction() { return prod; }
	public void unparse(CodeWriter cw) {
		cw.begin(3);		cw.write("ExtendCmd\n");		cw.end();
	}	
}
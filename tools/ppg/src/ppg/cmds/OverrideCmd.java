package jltools.util.jlgen.cmds;

import jltools.util.jlgen.atoms.*;import jltools.util.jlgen.util.*;

public class OverrideCmd implements Command
{
	private Production prod;
	
	public OverrideCmd(Production p) 
	{
		prod = p;
	}
	public void unparse(CodeWriter cw) {
		cw.begin(3);		cw.write("OverrideCmd");		cw.allowBreak(2);		cw.end();
	}	
}
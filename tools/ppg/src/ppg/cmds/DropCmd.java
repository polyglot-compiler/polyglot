package jltools.util.jlgen.cmds;

import java.util.*;
import jltools.util.jlgen.atoms.*;
import jltools.util.jlgen.util.*;
public class DropCmd implements Command
{
	private Production prod; // productions to be dropped for some nonterminal
	private Nonterminal nt; // or, the single nonterminal to be dropped
	
	public DropCmd(String nonterminal)
	{
		nt = new Nonterminal(nonterminal);
		prod = null;
	}
	
	public DropCmd(Production productions)
	{
		prod = productions;
		nt = null;
	}

	public void unparse(CodeWriter cw) {
		cw.begin(3);		cw.write("DropCmd\n");
		cw.end();
	}	
}
package jltools.util.jlgen.cmds;

import java.util.*;
import jltools.util.jlgen.util.*;

public class TransferCmd implements Command
{
	private String nonterminal;
	private Vector transferList;
	
	public TransferCmd(String nt, Vector tlist) {
		nonterminal = nt;
		transferList = tlist;
	}
	public void unparse(CodeWriter cw) {
		cw.begin(3);		cw.write("TransferCmd\n");		cw.end();
	}	
}
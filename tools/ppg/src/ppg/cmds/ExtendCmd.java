package polyglot.util.ppg.cmds;

import polyglot.util.ppg.atoms.*;import polyglot.util.ppg.util.*;

public class ExtendCmd implements Command
{
	private Production prod;
	
	public ExtendCmd(Production p)
	{
		prod = p;
	}	
	public Production getProduction() { return prod; }
	public void unparse(CodeWriter cw) {
		//cw.begin(0);		cw.write("ExtendCmd");
		cw.allowBreak(2);
		prod.unparse(cw);		//cw.end();
	}	
}
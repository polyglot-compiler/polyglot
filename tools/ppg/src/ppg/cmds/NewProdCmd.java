package polyglot.util.ppg.cmds;

import polyglot.util.ppg.atoms.*;
import polyglot.util.ppg.util.*;

public class NewProdCmd implements Command
{
	private Production prod;
	
	public NewProdCmd(Production p) 
	{
		prod = p;
	}

	public Production getProduction() { return prod; }
	
	public void unparse(CodeWriter cw) {
		//cw.begin(0);
		cw.write("NewProdCmd");
		cw.allowBreak(0);
		prod.unparse(cw);
		//cw.end();
	}
}
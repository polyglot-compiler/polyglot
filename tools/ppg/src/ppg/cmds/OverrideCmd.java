package polyglot.util.ppg.cmds;
import polyglot.util.ppg.atoms.*;import polyglot.util.ppg.util.*;

public class OverrideCmd implements Command
{
	private Production prod;
	
	public OverrideCmd(Production p) 
	{
		prod = p;
	}

	public Nonterminal getLHS() { return prod.getLHS(); }	public Production getProduction() { return prod; }
		public void unparse(CodeWriter cw) {
		//cw.begin(0);		cw.write("OverrideCmd");		cw.allowBreak(0);
		prod.unparse(cw);		//cw.end();
	}	
}
package jltools.util.jlgen.atoms;
import jltools.util.jlgen.parse.*;
import jltools.util.jlgen.util.*;

public class SemanticAction extends GrammarPart
{
	private String action;
	
	public SemanticAction (String actionCode) {
		action = actionCode;
	}
	public void unparse(CodeWriter cw) {
		cw.begin(0);		cw.write("{:");
		cw.allowBreak(0);
		cw.write(action);
		cw.allowBreak(0);
		cw.write(":}");		cw.end();
	}	
	public String toString () {
		return "{:" + action + ":}\n";
	}
}

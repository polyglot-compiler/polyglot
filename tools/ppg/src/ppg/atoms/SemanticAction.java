package jltools.util.jlgen.atoms;

public class SemanticAction
{
	private String action;
	
	public SemanticAction (String actionCode) {
		action = actionCode;
	}

	public String toString () {
		return "{:" + action + ":}\n";
	}
}

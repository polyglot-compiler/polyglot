package jltools.util.jlgen.code;

public class ActionCode extends Code
{
	private String action;
	
	public ActionCode (String actionCode) {
		action = actionCode;
	}
	public Object clone () {
		return new ActionCode(action.toString());	
	}	
	public String toString () {
		return "action code {:\n" + action + "\n:}\n";
	}
}

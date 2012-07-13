package ppg.atoms;

import java.util.*;

public class TerminalList
{
	private String type;
	private Vector<String> symbols;
	
	public TerminalList(String type, Vector<String> syms) {
		this.type = type;
		symbols = syms;
	}	
	
	public String toString() {
		String result = "TERMINAL ";
		if (type != null)
			result += type;
		
		for (int i=0; i < symbols.size(); i++) {
			result += symbols.elementAt(i);
			if (i < symbols.size() - 1)
				result += ", ";
		}
		return result + ";";
	}
}

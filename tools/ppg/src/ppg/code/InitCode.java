package jltools.util.jlgen.code;

public class InitCode extends Code
{
	private String init;
	
	public InitCode (String initCode) {
		init = initCode;
	}

	public String toString () {
		return "init code {:\n" + init + "\n:}\n";
	}
}


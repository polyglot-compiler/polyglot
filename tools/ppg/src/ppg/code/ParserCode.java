package jltools.util.jlgen.code;

public class ParserCode extends Code
{
	private String parser;
	
	public ParserCode (String parserCode) {
		parser = parserCode;
	}
	public Object clone () {
		return new ParserCode(parser.toString());	
	}
	public String toString () {
		return "parser code {:\n" + parser + "\n:}\n";
	}

}

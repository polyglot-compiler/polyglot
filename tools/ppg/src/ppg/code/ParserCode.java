package polyglot.util.ppg.code;

public class ParserCode extends Code
{
	public ParserCode (String parserCode) {
		value = parserCode;
	}
	public Object clone () {
		return new ParserCode(value.toString());	
	}
	public String toString () {
		return "parser code {:\n" + value + "\n:}\n";
	}

}

package jltools.util.jlgen.code;

public class ScanCode extends Code
{
	private String scan;
	
	public ScanCode (String scanCode) {
		scan = scanCode;
	}

	public Object clone () {
		return new ScanCode(scan.toString());	
	}	
	public String toString () {
		return "scan with {:" + scan + ":};";
	}

}

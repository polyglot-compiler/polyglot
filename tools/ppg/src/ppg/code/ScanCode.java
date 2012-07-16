package ppg.code;

public class ScanCode extends Code
{
	public ScanCode (String scanCode) {
		value = scanCode;
	}

	@Override
	public Object clone () {
		return new ScanCode(value.toString());	
	}
	
	@Override
	public String toString () {
		return "scan with {:" + value + ":};";
	}

}

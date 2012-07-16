package ppg.code;

public abstract class Code
{
	protected String value;
	@Override
	public abstract Object clone();
	
	public void append(String s) {
		value += "\n" + s;								  
	}
	
	public void prepend(String s) {
		value = s + "\n" + value;					   
	}
	
	@Override
	public abstract String toString();
}

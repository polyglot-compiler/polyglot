package ppg;

public class PPGError extends Throwable
{
	private String filename, error;
	private int line;
	
	public PPGError(String file, int lineNum, String errorMsg) {
		filename = file;
		line = lineNum;
		error = errorMsg;
	}
	
	public String getMessage() {
		return filename+": error on line "+String.valueOf(line)+": "+error;
	}
}

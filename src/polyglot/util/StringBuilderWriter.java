package polyglot.util;

import java.io.IOException;
import java.io.Writer;

public class StringBuilderWriter extends Writer {

	private final StringBuilder builder = new StringBuilder();
	
	@Override
	public void close() throws IOException {
		
	}

	@Override
	public void flush() throws IOException {

	}

	@Override
	public void write(char[] arg0, int arg1, int arg2) throws IOException {
		builder.append(arg0, arg1, arg2);
	}
	
	public StringBuilder getBuilder() {
		return builder;
	}

}

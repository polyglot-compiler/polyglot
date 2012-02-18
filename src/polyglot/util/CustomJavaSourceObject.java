package polyglot.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class CustomJavaSourceObject extends SimpleJavaFileObject {

	private StringWriter strWriter = new StringWriter();
	
	public CustomJavaSourceObject(String s) {
		super(URI.create("string:///" + s.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
	}
	
	@Override
	public Writer openWriter() throws IOException {
		return strWriter;
	}
	
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return strWriter.getBuffer();
	}
}

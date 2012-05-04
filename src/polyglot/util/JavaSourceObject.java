package polyglot.util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;

import javax.tools.JavaFileManager.Location;
import javax.tools.SimpleJavaFileObject;

public class JavaSourceObject extends SimpleJavaFileObject {

	//private StringWriter sw = new StringWriter();
	private final StringBuilderWriter sbw = new StringBuilderWriter();
	private final String fullName;
	
	public JavaSourceObject(String fullName) {
		super(URI.create(fullName + Kind.SOURCE.extension), Kind.SOURCE);
		this.fullName = fullName;
	}
	
	@Override
	public String getName() {
		return fullName + Kind.SOURCE.extension;
	}
	
	@Override
	public Writer openWriter() throws IOException {
		return sbw;
		//return sw;
	}
	
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return sbw.getBuilder();
		//return sw.getBuffer();
	}
}

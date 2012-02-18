package polyglot.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class CustomJavaClassObject extends SimpleJavaFileObject {
	
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	public CustomJavaClassObject(String s, Kind k) {
		super(URI.create("string:///" + s.replace('.', '/') + k.extension), k);
	}
	
	public byte[] getBytes() {
		return baos.toByteArray();
	}
	
	@Override
	public OutputStream openOutputStream() throws IOException {
		return baos;
	}
}
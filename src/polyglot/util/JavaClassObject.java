package polyglot.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import static java.io.File.separatorChar;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

public class JavaClassObject implements JavaFileObject {

	private FileOutputStream fos;
	private FileInputStream fis;
	private FileWriter fw;
	private FileReader fr;
	private String className;
	
	public JavaClassObject(String className) throws IOException {
		this.className = className;
		String filename = className + ".class";
		fos = new FileOutputStream(filename);
		fis = new FileInputStream(filename);
		fw = new FileWriter(filename);
		fr = new FileReader(filename);
	}
	
	public boolean delete() {
		fos = null;
		fis = null;
		fw = null;
		fr = null;
		return true;
	}

	public CharSequence getCharContent(boolean arg0) throws IOException {
		throw new UnsupportedOperationException();
	}

	public long getLastModified() {
		throw new UnsupportedOperationException();
	}

	public String getName() {
		return className;
	}

	public InputStream openInputStream() throws IOException {
		return fis;
	}

	public OutputStream openOutputStream() throws IOException {
		return fos;
	}

	public Reader openReader(boolean arg0) throws IOException {
		return fr;
	}

	public Writer openWriter() throws IOException {
		return fw;
	}

	public URI toUri() {
		return URI.create(className.replace('.', separatorChar) + Kind.CLASS.extension);
	}

	public Modifier getAccessLevel() {
		throw new UnsupportedOperationException();
	}

	public Kind getKind() {
		return Kind.CLASS;
	}

	public NestingKind getNestingKind() {
		throw new UnsupportedOperationException();
	}

	public boolean isNameCompatible(String arg0, Kind arg1) {
		String extension = arg0.substring(arg0.lastIndexOf('.') + 1);
		return extension.equals(arg1.extension);
	}

}

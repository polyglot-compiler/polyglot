package polyglot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.CharBuffer;

import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;

public class CustomFileObject implements FileObject {

	private File f;
	private FileOutputStream fos;
	private FileInputStream fis;
	private FileWriter fw;
	private FileReader fr;
	private StringWriter sw;
	private String filename;
	private boolean inMemory;
	private boolean isInputStream;
	private InputStream is;
	private URI uri;
	
	public CustomFileObject(String fullName, boolean inMemory) {
		this.inMemory = inMemory;
		if(inMemory) {
			uri = URI.create(fullName);
			sw = new StringWriter();
		} else
			f = new File(fullName);
		filename = fullName;
	}
	
	public CustomFileObject(String fullName, InputStream is) {
		isInputStream = true;
		uri = URI.create(fullName);
		inMemory = false;
		this.is = is;
		filename = fullName;
	}
	
	public boolean delete() {
		f = null;
		sw = null;
		return true;
	}

	public CharSequence getCharContent(boolean arg0) throws IOException {
		if(inMemory)
			return sw.getBuffer();
		if(f == null)
			throw new IOException();
		CharBuffer buf = CharBuffer.allocate((int)f.length());
		openReader(arg0).read(buf);
		return buf;
	}

	public long getLastModified() {
		return f != null ? f.lastModified() : -1;
	}

	public String getName() {
		return filename;
	}

	public InputStream openInputStream() throws IOException {
		if(inMemory)
			throw new UnsupportedOperationException();
		return f != null ? (fis == null ? new FileInputStream(f) : fis) : is;
	}

	public OutputStream openOutputStream() throws IOException {
		if(inMemory || f == null)
			throw new UnsupportedOperationException();
		return fos == null ? new FileOutputStream(f) : fos;
	}

	public Reader openReader(boolean arg0) throws IOException {
		if(inMemory)
			return new StringReader(sw.toString());
		if(f == null)
			throw new IOException();
		return fr == null ? new FileReader(f) : fr;
	}

	public Writer openWriter() throws IOException {
		if(inMemory)
			return sw;
		if(f == null)
			throw new IOException();
		return fw == null ? new FileWriter(f) : fw;
	}

	public URI toUri() {
		if(inMemory || f == null)
			return uri;
		return f.toURI();
	}

}

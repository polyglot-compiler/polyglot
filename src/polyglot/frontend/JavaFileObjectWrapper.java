package polyglot.frontend;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.FileObject;
import javax.tools.ForwardingFileObject;
import javax.tools.JavaFileObject;

public class JavaFileObjectWrapper extends ForwardingFileObject<FileObject> implements JavaFileObject {
	protected JavaFileObject.Kind kind;
	/**
	 * Override the extension in 'kind'
	 */
	protected String extension;
	protected String simpleName;
	
	protected JavaFileObjectWrapper(FileObject fileObject, String extension, JavaFileObject.Kind kind) {
		super(fileObject);
		this.extension = extension;
		URI uri = fileObject.toUri();
		this.simpleName = new File(uri.getPath()).getName();
		this.kind = kind;
	}

	public Modifier getAccessLevel() {
		return null;
	}

	public Kind getKind() {
		return kind;
	}

	public NestingKind getNestingKind() {
		return null;
	}

	public boolean isNameCompatible(String simpleName, Kind kind) {
		return (this.simpleName.equals(simpleName) && this.kind.equals(kind));
	}

	@Override
	public boolean equals(Object obj) {
		System.out.println("WRAPPED SOURCE " + this + " equal to " + obj +  "? " + this.fileObject.equals(obj));
		return this.fileObject.equals(obj);	
	}

	@Override
	public int hashCode() {
		return this.fileObject.hashCode();
	}
	
}

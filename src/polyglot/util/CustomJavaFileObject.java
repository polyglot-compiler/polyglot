package polyglot.util;

import java.io.IOException;
import java.io.InputStream;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;

public class CustomJavaFileObject extends CustomFileObject implements
		JavaFileObject {

	private Kind k;

	public CustomJavaFileObject(String fullname, Kind kind, boolean inMemory) {
		super(fullname, inMemory);
		k = kind;
	}
	
	public CustomJavaFileObject(String fullname, Kind kind, InputStream is) {
		super(fullname, is);
		k = kind;
	}
	
	public Modifier getAccessLevel() {
		throw new UnsupportedOperationException();
	}

	public Kind getKind() {
		return k;
	}

	public NestingKind getNestingKind() {
		throw new UnsupportedOperationException();
	}

	public boolean isNameCompatible(String arg0, Kind arg1) {
		return arg0.substring(arg0.lastIndexOf('.') + 1).equals(arg1.extension);
	}

}

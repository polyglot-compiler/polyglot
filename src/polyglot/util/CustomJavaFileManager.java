package polyglot.util;

import java.io.IOException;
import java.security.SecureClassLoader;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

public class CustomJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

	private CustomJavaClassObject classObject;
	
	public CustomJavaFileManager(StandardJavaFileManager fm) {
		super(fm);
	}
	
	@Override
    public ClassLoader getClassLoader(Location location) {
        return new SecureClassLoader() {
            @Override
            protected Class<?> findClass(String name)
                throws ClassNotFoundException {
                byte[] b = classObject.getBytes();
                return super.defineClass(name, b, 0, b.length);
            }
        };
    }
	
	@Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
		classObject = new CustomJavaClassObject(className, kind);
        return classObject;
    }
}

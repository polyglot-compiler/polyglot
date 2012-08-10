package polyglot.filemanager;

import java.net.URI;
import java.util.Map;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import polyglot.frontend.SourceLoader;
import polyglot.types.reflect.ClassFileLoader;

public interface FileManager extends StandardJavaFileManager, SourceLoader,
        ClassFileLoader {
    Map<URI, JavaFileObject> getAbsPathObjMap();
}

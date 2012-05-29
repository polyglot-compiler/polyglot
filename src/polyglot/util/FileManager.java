package polyglot.util;

import javax.tools.StandardJavaFileManager;

import polyglot.frontend.SourceLoader;
import polyglot.types.reflect.ClassFileLoader;

public interface FileManager extends StandardJavaFileManager, SourceLoader, ClassFileLoader {

}

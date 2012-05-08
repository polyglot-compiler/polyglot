package polyglot.util;

import javax.tools.StandardJavaFileManager;

import polyglot.frontend.SourceLoader;
import polyglot.types.reflect.ClassFileLoader;

/**
 * Interface for a file manager implementation (NOTE: Extensions may extend this
 * interface for having additional functionality.)
 */
public interface FileManager extends StandardJavaFileManager, SourceLoader,
		ClassFileLoader {

}

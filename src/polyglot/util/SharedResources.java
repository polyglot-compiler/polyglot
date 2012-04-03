package polyglot.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.tools.JavaFileObject;

public class SharedResources {
	/** Map used by CustomExtFileManager and CustomJavaFileManager_. It stores fully qualified package names and contained JavaFileObjects */ 
	static final Map<String, Set<JavaFileObject>> pathObjectMap = new HashMap<String, Set<JavaFileObject>>();
}

package polyglot.types.reflect;

import javax.tools.JavaFileManager.Location;

public interface ClassFileLoader {
	boolean packageExists(String name);
	ClassFile loadFile(String name);
	void addLocation(Location loc);
}

package polyglot.types.reflect;

import javax.tools.JavaFileManager.Location;

public interface ClassFileLoader {
	boolean packageExists(String name);
	boolean packageExists(Location location, String name);
	ClassFile loadFile(String name);
	ClassFile loadFile(Location location, String name);
	void addLocation(Location loc);
}

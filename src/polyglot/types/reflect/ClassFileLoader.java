package polyglot.types.reflect;

public interface ClassFileLoader {
	boolean packageExists(String name);
	ClassFile loadFile(String name);

}

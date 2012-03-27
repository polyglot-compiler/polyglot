package insens;

// linking against a classfile of a package with the same name as class breaks
// on case-insensitive filesystems.  Compile Insens.fab before compiling this.
public class InsensBug {
	public insens.Insens bar;
}

package polyglot.util;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.*;

/** An enumerated type.  Enums are interned and can be compared with ==. */
public class Enum implements Serializable
{
    /** The name of the enum--for debugging only. */
    private String name;

    /** A key used to distinguish objects of the same type. */
    private int key;

    /** The next key to allocate. */
    private static int next = 0;

    /** The intern cache. */
    private static Map cache = new HashMap();

    protected Enum(String name) {
	this.name = name;
	this.key = next++;
    }

    /** For serialization. */
    private Enum() {
    }

    public boolean equals(Object o) {
	return this == o;
    }

    public String toString() {
	return name;
    }

    private static class EnumKey {
	Enum e;

	EnumKey(Enum e) {
	    this.e = e;
	}

	public boolean equals(Object o) {
	    return o instanceof Enum
	        && e.key == ((Enum) o).key
	        && e.getClass() == o.getClass();
	}

	public int hashCode() {
	    return e.getClass().hashCode() ^ e.key;
	}
    }

    public Enum intern() {
        EnumKey k = new EnumKey(this);

	Enum e = (Enum) cache.get(k);

	if (e == null) {
	    cache.put(k, this);
	    return this;
	}

	return e;
    }

    private Object readObject(ObjectInputStream in)
	throws IOException, ClassNotFoundException {

        String n = in.readUTF();
	int k = in.readInt();
	Enum e = new Enum();
	e.name = n;
	e.key = k;
	return e.intern();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(name);
	out.writeInt(key);
    }
}

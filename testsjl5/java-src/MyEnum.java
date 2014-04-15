import java.io.Serializable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;

public abstract class MyEnum<E extends MyEnum<E>>
        implements Comparable<E>, Serializable {
    private final String name;
    public String name() {
        return name;
    }

    private final int ordinal;
    public final int ordinal() {
        return ordinal;
    }
    
    private static Map<Class,Map<String,MyEnum>> enumDirectory = new HashMap<Class,Map<String,MyEnum>>();
    
    private static void addToEnumDirectory(Class enumClass, String name, MyEnum object) {
        if (!enumDirectory.containsKey(enumClass)) {
            enumDirectory.put(enumClass, new HashMap<String,MyEnum>());
        }
        enumDirectory.get(enumClass).put(name, object);
    }
    
    private static MyEnum getFromEnumDirectory(Class enumClass, String name) {
        if (!enumDirectory.containsKey(enumClass)) return null;
        return enumDirectory.get(enumClass).get(name);
    }

    protected MyEnum(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
        addToEnumDirectory(this.getDeclaringClass(), name, this);
    }

    public String toString() {
        return name;
    }

    public final boolean equals(Object other) {
        return this==other;
    }

    public final int hashCode() {
        return super.hashCode();
    }

    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public final int compareTo(E o) {
        MyEnum other = (MyEnum)o;
        if (this.getDeclaringClass() != other.getDeclaringClass())
            throw new ClassCastException();
        return this.ordinal - other.ordinal;
    }

    public final Class<E> getDeclaringClass() {
        Class clazz = getClass();
        Class zuper = clazz.getSuperclass();
        return (zuper == MyEnum.class) ? clazz : zuper;
    }

    public static <T extends MyEnum<T>> T valueOf(Class<T> enumType,
                                                String name) {
        T result = (T) MyEnum.getFromEnumDirectory(enumType, name);
        if (result != null)
            return result;
        if (name == null)
            throw new NullPointerException("Name is null");
        throw new IllegalArgumentException(
            "No enum const " + enumType +"." + name);
    }


    /**
     * prevent default deserialization
     */
    private void readObject(ObjectInputStream in) throws IOException,
        ClassNotFoundException {
            throw new InvalidObjectException("can't deserialize enum");
    }

    private void readObjectNoData() throws ObjectStreamException {
            throw new InvalidObjectException("can't deserialize enum");
    }
}

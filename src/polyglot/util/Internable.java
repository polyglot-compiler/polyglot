package polyglot.util;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.*;

/** An Internable object.  intern() is called during deserialization. */
public interface Internable
{
    public Object intern();
}

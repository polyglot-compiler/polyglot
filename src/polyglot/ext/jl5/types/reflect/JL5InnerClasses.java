package polyglot.ext.jl5.types.reflect;

import java.io.DataInputStream;
import java.io.IOException;

import polyglot.types.reflect.InnerClasses;

public class JL5InnerClasses extends InnerClasses {

    public JL5InnerClasses(DataInputStream in, int nameIndex, int length)
            throws IOException {
        super(in, nameIndex, length);
    }

}
